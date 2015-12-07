(ns cljstone.minion-test
  (:require [schema.core :as s]
            [cljs.test :refer-macros [deftest testing is use-fixtures]])
  (:use [cljstone.bestiary :only [all-minions]]
        [cljstone.card :only [Card]]
        [cljstone.character :only [get-health has-summoning-sickness? can-attack?]]
        [cljstone.minion :only [Minion MinionSchematic make-minion]]
        [cljstone.test-helpers :only [get-minion-card fresh-board]]
        [plumbing.core :only [safe-get safe-get-in]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(deftest calculating-health
  (testing "base health, no modifiers"
    (let [minion (-> all-minions :wisp (make-minion 123))]
      (is (= (get-health minion) 1))))

  (testing "buffed and attacked"
    (let [minion (-> all-minions
                     :boulderfist-ogre
                     (make-minion 123)
                     (update-in [:modifiers] conj {:type :mechanic :name "foo" :effect {:health 1}})
                     (update-in [:modifiers] conj {:type :attack :effect {:health -2}}))]
      (is (= (get-health minion) 6))))

  (testing "health after a couple of attacks"
    (let [minion (-> all-minions
                     :boulderfist-ogre
                     (make-minion 123)
                     (update-in [:modifiers] conj {:type :attack :effect {:health -2}})
                     (update-in [:modifiers] conj {:type :attack :effect {:health -3}}))]
      (is (= (get-health minion) 2)))))

(s/defn play-and-get :- Minion
  [card :- Card]
  (-> fresh-board
      ((:effect card) :player-1 card)
      (safe-get-in [:player-1 :minions 0])))

(deftest summoning-sickness
  (is (= (has-summoning-sickness? (play-and-get (get-minion-card :boulderfist-ogre)))
          true))

  (is (= (can-attack? (play-and-get (get-minion-card :boulderfist-ogre)))
          false))

  (is (= (has-summoning-sickness? (play-and-get (get-minion-card :bluegill-warrior)))
         false))

  (is (= (can-attack? (play-and-get (get-minion-card :bluegill-warrior)))
         true)))
