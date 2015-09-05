(ns cljstone.hero
  (:require [schema.core :as s]))

(def Hero
  {:name s/Str
   :class s/Keyword
   :health s/Int
   :id s/Int})

; todo hero powers
; todo class enum - see how you can do a set of specific values in schema

(s/defn make-hero :- Hero [hero-name hero-class hero-id]
  {:name hero-name :class hero-class :health 30 :id hero-id})
