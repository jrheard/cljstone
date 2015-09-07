(ns cljstone.board-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljstone.hero :as h]
            [cljstone.minion :as m]
            [schema.core :as s])
  (:use [cljstone.app :only [make-random-deck]]
        [cljstone.board :only [path-to-character make-board play-card BoardHalf end-turn]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.combat :only [attack]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(def hero-1 (h/make-hero "Jaina" :mage (get-next-character-id)))
(def hero-2 (h/make-hero "Thrall" :shaman (get-next-character-id)))
(def board (make-board hero-1 (make-random-deck) hero-2 (make-random-deck)))

(deftest finding-paths
  (testing "looking up heroes"
    (is (= (path-to-character board (:id hero-1))
           [:player-1 :hero])
        (= (path-to-character board (:id hero-2))
           [:player-2 :hero])))

  (testing "looking up minions"
   (let [board (-> board
                   (play-card :player-1 0)
                   (play-card :player-1 0)
                   (play-card :player-2 0))
         minion-to-find (get-in board [:player-1 :minions 1])]
     (is (= (path-to-character board (:id minion-to-find))
            [:player-1 :minions 1])))))

(deftest turns
  (testing "turns existing"
    (is (= (:turn board) 0))

    (let [board (assoc board :whose-turn :player-1)
          board (end-turn board)]
      (is (= (:turn board 1)))
      (is (= (:whose-turn board) :player-2))))

  (testing "resetting minions' number of attacks this turn"
    (let [board (-> board
                    (assoc :whose-turn :player-1)
                    (assoc-in [:player-1 :minions 0] (m/make-minion (:river-crocilisk m/all-minions) 123))
                    (assoc-in [:player-2 :minions 0] (m/make-minion (:river-crocilisk m/all-minions) 234)))]
      ; player 1 and player 2 each have a river croc.
      (is (= true (m/can-attack (get-in board [:player-1 :minions 0]))))

      (let [board (attack board 123 234)]
        ; player 1's croc attacks player 2's croc; it can only attack once per turn, so it can't attack again.
        (is (= false (m/can-attack (get-in board [:player-1 :minions 0]))))

        (let [board (end-turn board)]
          ; once player 1 hits "end turn", though, the croc can attack again the next time it's p1's turn.
          (is (= true (m/can-attack (get-in board [:player-1 :minions 0])))))))))


; TODO test playing cards
