(ns cljstone.hero
  (:require [schema.core :as s]))

(def Hero
  {:name s/Str
   :class s/Keyword
   :health s/Int})

; todo hero powers
; todo class enum - see how you can do a set of specific values in schema

(s/defn make-hero :- Hero [name hero-class]
  {:name name :class hero-class :health 30})

(def jaina (make-hero "Jaina" :mage))
(def thrall (make-hero "Thrall" :shaman))
