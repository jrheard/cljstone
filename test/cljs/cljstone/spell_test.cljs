(ns cljstone.spell-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljstone.spell :as s])
  (:use [schema.test :only [validate-schemas]]
        [cljstone.test-helpers :only [three-minions-per-player-board]]
        [cljstone.board :only [make-board play-card]]))

(use-fixtures :once validate-schemas)

(deftest flamecannon
  (let [board (assoc-in three-minions-per-player-board
                        [:player-1 :hand 0]
                        (s/spell->card (s/all-spells :flamecannon)))]
    (with-redefs [rand-nth first]
      (let [board (play-card board :player-1 0)]
        (is (= (get-in board [:player-2 :minions 0 :modifiers])
               [{:type :damage-spell :name "Flamecannon" :effect {:health -4}}]))))))
