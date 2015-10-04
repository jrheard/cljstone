(ns cljstone.combat-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljstone.minion :as m]
            [schema.core :as s])
  (:use [cljstone.bestiary :only [all-minions]]
        [cljstone.board :only [play-card path-to-character]]
        [cljstone.character :only [get-health]]
        [cljstone.combat :only [attack find-dead-characters-in-board remove-minion]]
        [cljstone.combat-log :only [get-next-log-entry-id]]
        [cljstone.test-helpers :only [fresh-board hero-1 hero-2 three-minions-per-player-board]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(deftest attacking
  (testing "two minions attacking each other"
    (with-redefs [get-next-log-entry-id (fn [] 0)]
      (let [board (-> fresh-board
                      (update-in [:player-1 :minions] conj (m/make-minion (:boulderfist-ogre all-minions) 123))
                      (update-in [:player-2 :minions] conj (m/make-minion (:war-golem all-minions) 234)))
            ogre (get-in board (path-to-character board 123))
            golem (get-in board (path-to-character board 234))
            board (attack board 123 234)]
        ; ogre dies, war golem survives with 1 health
        (is (= (get-in board [:player-1 :minions 0])
               nil))
        (is (= (get-health (get-in board [:player-2 :minions 0]))
               1))

        ; combat log recorded both of those damages
        (is (= (-> board :combat-log first)
               {:modifier {:type :attack :effect {:health -6}} :id 0 :source nil :target golem}))
        (is (= (-> board :combat-log (nth 1))
               {:modifier {:type :attack :effect {:health -7}} :id 0 :source nil :target ogre})))))

  (testing "both minions kill each other"
    (let [board (-> fresh-board
                      (update-in [:player-1 :minions] conj (m/make-minion (:war-golem all-minions) 123))
                      (update-in [:player-2 :minions] conj (m/make-minion (:war-golem all-minions) 234)))
            ogre (get-in board (path-to-character board 123))
            golem (get-in board (path-to-character board 234))
            board (attack board 123 234)]
        ; both die
        (is (= (get-in board [:player-1 :minions 0])
               nil))
        (is (= (get-in board [:player-2 :minions 0])
               nil)))))

(deftest playing-cards
  (testing "playing a minion"
    (let [card (m/minion-schematic->card (:wisp all-minions))
          board (-> fresh-board
                    (assoc-in [:player-1 :hand 0] card)
                    (play-card :player-1 0))]
      (is (= (get-in board [:player-1 :minions 0 :name])
             "Wisp")))))

(deftest find-dead-character
  (testing "no dead characters"
    (is (= (find-dead-characters-in-board fresh-board) [])))

  (let [card (first (get-in fresh-board [:player-1 :hand]))
        board (-> fresh-board
                  (play-card :player-1 0)
                  (assoc-in [:player-1 :minions 0 :base-health] 0))]

    (testing "one dead character"
      (is (= (count (find-dead-characters-in-board board)) 1))

      (is (= (:name card)
             (:name (nth (find-dead-characters-in-board board) 0))))))

  (let [board (-> fresh-board
                  (play-card :player-1 0)
                  (play-card :player-1 0)
                  (assoc-in [:player-1 :minions 0 :base-health] 0)
                  (assoc-in [:player-1 :minions 1 :base-health] 0))]
  ; xxx is left-to-right the correct order to seek dead minions? probably not, right?
  ; should be sorting by id, not board position - update this test when we implement deathrattles (and playing a minion at a position) and it starts mattering

  (testing "if there are two dead characters, we should get both"
    (is (= (count (find-dead-characters-in-board board)) 2))

    (is (= (:id (first (find-dead-characters-in-board board)))
           (:id (get-in board [:player-1 :minions 0]))))

    (is (= (:id (second (find-dead-characters-in-board board)))
           (:id (get-in board [:player-1 :minions 1])))))))

(deftest removing-minions
  (let [board three-minions-per-player-board
        player-1-minions (get-in board [:player-1 :minions])]
  (is (= (get-in (remove-minion board (:id (nth player-1-minions 1)))
                 [:player-1 :minions])
         (concat (subvec player-1-minions 0 1)
                 (subvec player-1-minions 2 3))))))
