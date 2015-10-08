(ns tools.card-scraper
  (:require [cheshire.core :refer [parse-string]]))


(defonce contents (-> "http://hearthstonejson.com/json/AllSets.json"
                      slurp
                      (parse-string true)))

(defn parse-card [card]
  (select-keys card [:faction :name :type :mechanics :health :attack :cost :text :playerClass]))

(defn get-card-set [card-set-kw]
  (->> contents
       card-set-kw
       (filter :collectible)
       (filter #(not= (:type %) "Hero"))
       (map parse-card)))

(def basic (get-card-set :Basic))

(def vanilla-basic (filter #(not (contains? % :text)) basic))

(def basic-minions (filter #(= (:type %) "Minion") basic))


(comment
  (prn (keys contents))

  (count basic)

  (take 10 (drop 10 basic))

  (prn basic-minions)

  )

