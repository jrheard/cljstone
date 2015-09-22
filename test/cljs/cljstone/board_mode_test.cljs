(ns cljstone.board-mode-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [schema.core :as s])
  (:use [cljstone.board-mode :only [BoardMode]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(s/defn mode-identity :- BoardMode
  [mode :- BoardMode]
  mode)

(deftest board-modes
  (is (= {:type :default}
         (mode-identity {:type :default})))

  (is (= 'foo
         (mode-identity {:type :default :foo :bar}))))
