(ns cljstone.spell-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]])
  (:use [schema.test :only [validate-schemas]]
        [cljstone.bestiary :only [all-minions]]
        [cljstone.board :only [make-board play-card run-continuation]]
        [cljstone.character :only [get-health]]
        [cljstone.minion :only [make-minion]]
        [cljstone.spell :only [spell->card]]
        [cljstone.spellbook :only [all-spells]]
        [cljstone.test-helpers :only [fresh-board three-minions-per-player-board]]
        [cljstone.utils :only [get-next-id]]
        [plumbing.core :only [safe-get safe-get-in]]))

(use-fixtures :once validate-schemas)

(deftest arcane-intellect
  (let [original-hand-size (count (safe-get-in fresh-board [:player-1 :hand]))
        board (-> fresh-board
                  (assoc-in [:player-1 :hand 0] (spell->card (all-spells :arcane-intellect)))
                  (play-card :player-1 0))]
    (is (= (count (safe-get-in board [:player-1 :hand]))
           (+ original-hand-size 1)))))

(deftest flamecannon
  (let [board (assoc-in three-minions-per-player-board
                        [:player-1 :hand 0]
                        (spell->card (all-spells :flamecannon)))
        board (assoc-in board [:player-2 :minions 0] (make-minion (:war-golem all-minions) 123))]
    (with-redefs [rand-nth first
                  get-next-id (fn [] 0)]
      (let [minion (get-in board [:player-2 :minions 0])
            board (play-card board :player-1 0)]
        (is (= (get-in board [:player-2 :minions 0 :modifiers])
               [{:type :damage-spell :name "Flamecannon" :effect {:health -4}}]))

        (is (= (-> board :combat-log first)
               {:modifier {:type :damage-spell :name "Flamecannon" :effect {:health -4}} :id 0 :source nil :target minion}))))))

(deftest fireball
  (let [board (-> fresh-board
                  (assoc-in [:player-1 :hand 0] (spell->card (all-spells :fireball)))
                  (play-card :player-1 0))]
    (is (= (safe-get-in board [:mode :type])
           :targeting))

    (is (= (safe-get-in board [:mode :targets])
           [(safe-get-in board [:player-1 :hero])
            (safe-get-in board [:player-2 :hero])]))

    (let [board (run-continuation board (safe-get-in board [:player-1 :hero]))]
      (is (= (get-health (safe-get-in board [:player-1 :hero]))
             24)))))
