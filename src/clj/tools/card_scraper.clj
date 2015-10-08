(ns tools.card-scraper
  (:require [cheshire.core :refer [parse-string]]))


(defonce contents (parse-string (slurp "http://hearthstonejson.com/json/AllSets.json") true))

(comment
  (prn (keys contents))

  )

