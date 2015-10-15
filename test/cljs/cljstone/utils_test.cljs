(ns cljstone.utils-test
  (:require [cljs.test :refer-macros [deftest is use-fixtures]])
  (:use [cljstone.utils :only [get-next-id]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(deftest test-ids
  (let [an-id (get-next-id)
        another-id (get-next-id)]
    (is (> another-id an-id))))
