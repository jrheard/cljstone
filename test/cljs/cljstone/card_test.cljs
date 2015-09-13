(ns cljstone.card-test
  (:require [cljs.test :refer-macros [deftest is use-fixtures]])
  (:use [cljstone.card :only [get-next-card-id]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(deftest test-card-ids
  (let [an-id (get-next-card-id)
        another-id (get-next-card-id)]
    (is (> another-id an-id))))
