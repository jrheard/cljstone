(ns cljstone.spell-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]])
  (:use [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

