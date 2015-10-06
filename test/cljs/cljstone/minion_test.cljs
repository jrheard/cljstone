(ns cljstone.minion-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]])
  (:use [cljstone.bestiary :only [all-minions]]
        [cljstone.character :only [get-health]]
        [cljstone.minion :only [make-minion]]
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
                     (update-in [:modifiers] conj {:type :buff :name "foo" :effect {:base-health 1}})
                     (update-in [:modifiers] conj {:type :attack :effect {:health -2}}))]
      (is (= (get-health minion) 6))))

  (testing "health after a couple of attacks"
    (let [minion (-> all-minions
                     :boulderfist-ogre
                     (make-minion 123)
                     (update-in [:modifiers] conj {:type :attack :effect {:health -2}})
                     (update-in [:modifiers] conj {:type :attack :effect {:health -3}}))]
      (is (= (get-health minion) 2)))))
