(ns cljstone.card-test
  (:require [cljs.test :refer-macros [deftest is use-fixtures]])
  (:use [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)
