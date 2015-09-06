(ns cljstone.combat-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljstone.card :as c]
            [cljstone.hero :as h]
            [cljstone.minion :as m]
            [schema.core :as s])
  (:use [cljstone.board :only [play-card BoardHalf]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.combat :only [attack]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(def hero-1 (h/make-hero "Jaina" :mage (get-next-character-id)))
(def hero-2 (h/make-hero "Thrall" :shaman (get-next-character-id)))

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
    (let [card (c/minion-schematic->card (:wisp m/all-minions))
          board {:player-1 (make-test-board-half {:hero hero-1 :hand [card]})
                 :player-2 (make-test-board-half {:hero hero-2})
                 :whose-turn :player-1
                 :turn 0}
          post-play-board (play-card board :player-1 0)]
      (is (= (get-in post-play-board [:player-1 :minions 0 :name])
             "Wisp")))))
