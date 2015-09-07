(ns cljstone.combat-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljstone.hero :as h]
            [cljstone.minion :as m]
            [schema.core :as s])
  (:use [cljstone.app :only [make-random-deck]]
        [cljstone.board :only [make-board play-card BoardHalf]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.combat :only [attack find-a-dead-character-in-board remove-minion]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(def hero-1 (h/make-hero "Jaina" :mage (get-next-character-id)))
(def hero-2 (h/make-hero "Thrall" :shaman (get-next-character-id)))
(def board (make-board hero-1 (make-random-deck) hero-2 (make-random-deck)))

(s/defn make-test-board-half :- BoardHalf
  [{:keys [hero hand deck minions] :or {hand [] deck [] minions []}}]
  {:hero hero :hand hand :deck deck :minions minions})

(deftest attacking
  (testing "two minions attacking each other"
    (let [board {:player-1 (make-test-board-half {:hero hero-1 :minions [(m/make-minion (:boulderfist-ogre m/all-minions) 123)]})
                 :player-2 (make-test-board-half {:hero hero-2 :minions [(m/make-minion (:war-golem m/all-minions) 234)]})
                 :whose-turn :player-1
                 :turn 0}
          post-attack-board (attack board 123 234)]
      ; ogre dies, war golem survives with 1 health
      (is (= (m/get-health (get-in post-attack-board [:player-1 :minions 0]))
             0))
      (is (= (m/get-health (get-in post-attack-board [:player-2 :minions 0]))
             1)))))

(deftest playing-cards
  (testing "playing a minion"
    (let [card (m/minion-schematic->card (:wisp m/all-minions))
          board {:player-1 (make-test-board-half {:hero hero-1 :hand [card]})
                 :player-2 (make-test-board-half {:hero hero-2})
                 :whose-turn :player-1
                 :turn 0}
          post-play-board (play-card board :player-1 0)]
      (is (= (get-in post-play-board [:player-1 :minions 0 :name])
             "Wisp")))))

(deftest find-dead-character
  (testing "no dead characters"
    (is (= (find-a-dead-character-in-board board) nil)))

  (let [card (first (get-in board [:player-1 :hand]))
        board (-> board
                  (play-card :player-1 0)
                  (assoc-in [:player-1 :minions 0 :base-health] 0))]

    (testing "one dead character"
      (is (= (:name card)
             (:name (find-a-dead-character-in-board board)))))

    (let [board (-> board
                    (play-card :player-1 0)
                    (assoc-in [:player-1 :minions 1 :base-health] 0))
          first-minion (get-in board [:player-1 :minions 0])]
    ; xxx is left-to-right the correct order to seek dead minions? probably not, right?
    ; should be sorting by id, not board position - update this test when we implement deathrattles (and playing a minion at a position) and it starts mattering
    (testing "if there are two dead characters, we should get the first"
      (is (= (:base-health first-minion) 0))
      (is (= (get-in board [:player-1 :minions 1 :base-health]) 0))
      (is (= (:id (find-a-dead-character-in-board board))
             (:id first-minion)))))))

(deftest removing-minions
  (let [board (-> board
                  (play-card :player-1 0)
                  (play-card :player-2 0)
                  (play-card :player-1 0))
        player-1-minions (get-in board [:player-1 :minions])]
  (is (= (get-in (remove-minion board (:id (nth player-1-minions 1)))
                 [:player-1 :minions])
         (subvec player-1-minions 0 1)))))
