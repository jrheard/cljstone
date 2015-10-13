(ns cljstone.spell-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]])
  (:use [schema.test :only [validate-schemas]]
        [cljstone.bestiary :only [all-minions]]
        [cljstone.test-helpers :only [three-minions-per-player-board]]
        [cljstone.board :only [make-board play-card]]
        [cljstone.minion :only [make-minion]]
        [cljstone.spell :only [spell->card]]
        [cljstone.spellbook :only [all-spells]]
        [cljstone.utils :only [get-next-id]]))

(use-fixtures :once validate-schemas)

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
