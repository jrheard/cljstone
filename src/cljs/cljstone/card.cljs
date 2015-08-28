(ns cljstone.card
  (:require [schema.core :as s]))

; todo
; a card is a name, a mana cost, and an effect function
; also a type, spell or minion
; minions all have the same effect function - (summon-minion the-minion)

; todo - how will freezing trap / thaurissan work?

; TODO - perhaps TargetingStrategies for battlecries/spells/attacking - minion attacking takes taunt into account, spells/battlecries don't

; TODO - on-before-attack for ogre brute, on-after-attack for mistress of pain
