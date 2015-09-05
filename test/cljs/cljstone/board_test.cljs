(ns cljstone.board-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljstone.hero :as hero])
  (:use [cljstone.board :only [find-a-dead-character-in-board make-board play-card]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(def hero-1 (hero/make-hero "Jaina" :mage 0))
(def hero-2 (hero/make-hero "Thrall" :shaman 1))
(def board @(make-board hero-1 hero-2))

(deftest find-dead-character
  (testing "no dead characters"
    (is (= (find-a-dead-character-in-board board) nil)))

  (let [board (-> board
                  (play-card :player-1 0)
                  (assoc-in [:player-1 :minions 0 :base-health] 0))]
    (testing "one dead character"
      (is (not (= (find-a-dead-character-in-board board)
                  nil))))

    (let [board (-> board
                    (play-card :player-1 0)
                    (assoc-in [:player-1 :minions 1 :base-health] 0))
          first-minion (get-in board [:player-1 :minions 0]) ]
    ; xxx is left-to-right the correct order to seek dead minions? probably not, right?
    ; should be sorting by id, not board position - update this test when we implement deathrattles (and playing a minion at a position) and it starts mattering
    (testing "if there are two dead characters, we should get the first"
      (is (= (:base-health first-minion) 0))
      (is (= (get-in board [:player-1 :minions 1 :base-health]) 0))
      (is (= (:id (find-a-dead-character-in-board board))
             (:id first-minion)))))))
