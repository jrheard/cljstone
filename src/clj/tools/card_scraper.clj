(ns tools.card-scraper
  (:require [cheshire.core :refer [parse-string]]
            [clojure.set :refer [rename-keys]]))

(defonce contents (-> "http://hearthstonejson.com/json/AllSets.json"
                      slurp
                      (parse-string true)))

(defn parse-card [card]
  (select-keys card [:faction :name :type :mechanics :health :attack :cost :text :playerClass]))

(defn parse-minion [minion]
  (-> minion
      (dissoc :type :faction)
      (rename-keys {:attack :base-attack
                    :health :base-health
                    :cost :mana-cost})))

(defn get-card-set [card-set-kw]
  (->> contents
       card-set-kw
       (filter :collectible)
       (filter #(not= (:type %) "Hero"))
       (map parse-card)))

(def basic (get-card-set :Basic))

(def vanilla-basic (filter #(not (contains? % :text)) basic))

(def basic-minions (->> basic
                        (filter #(= (:type %) "Minion"))
                        (map parse-minion)))

(def simple-taunt-minions (filter #(= (:mechanics %) ["Taunt"]) basic-minions))


(comment
  (prn (keys contents))

  (count basic)

  (take 10 (drop 10 basic))

  (prn simple-taunt-minions)

  )

