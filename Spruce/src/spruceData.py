# -*- coding: utf-8 -*-
# Leisure is a limp spruce! by praetor_alpha
# data module

from spruceNoun import Pronoun
from spruceNoun import Adverb
from spruceNoun import Adjective
from spruceNoun import Noun
from spruceVerb import Verb
from spruceParts import Interjection
from spruceParts import Preposition
from spruceParts import Conjunction
from xml.dom import minidom
#from xml.dom import Node
from spruceNoun import *
from spruceVerb import *

class XMLloader(object):
    doc=None
    classes={Verb.abbrev:Verb, Noun.abbrev:Noun,
            Adjective.abbrev:Adjective, Adverb.abbrev:Adverb,
            Conjunction.abbrev:Conjunction, Pronoun.abbrev:Pronoun,
            Preposition.abbrev:Preposition, Interjection.abbrev:Interjection}
    def __init__(self, XML, isFile=True, includeSpecial=True):
        """run through entire XML, and put into lists (roughly) based on parts of speech"""
        self.masterVerbs=[]
        if includeSpecial:
            self.verbs=[vBe, vHave]
            self.hVerbs=[vBe, vHave]
            self.lVerbs=[vBe, vHave]
        else:
            self.verbs=[]
            self.hVerbs=[]
            self.lVerbs=[]
        self.nouns=[]
        self.adjs=[]
        self.advs=[]
        self.preps=[]
        self.conjs=[]
        self.interjs=[]
        self.pros=[]
        self.words=[]
        self.subjects=[]
        self.parts={Verb.abbrev:self.masterVerbs, Noun.abbrev:self.nouns,
            Adjective.abbrev:self.adjs, Adverb.abbrev:self.advs,
            Conjunction.abbrev:self.conjs, Pronoun.abbrev:self.pros,
            Preposition.abbrev:self.preps, Interjection.abbrev:self.interjs}
        if isFile:
            self.doc=minidom.parse(XML)
        else:
            self.doc=minidom.parseString(XML)
        for word in self.doc.documentElement.childNodes:
            try:
                self.addWord(word, word.nodeName)
            except: pass    # sometimes nodes are '#text'
        self.sortItAllOut()
        self.subjects=self.nouns[:]
        if includeSpecial:
            self.subjects.append(You())
    def addWord(self, word, abbrev):
        """adds any word as the given part of speech abbreviation"""
        instan=self.classes[abbrev](word)
        self.parts[abbrev].append(instan)
        if abbrev==Verb.abbrev:
            if instan.infinitive:
                self.verbs.append(instan)
            if instan.linking:
                self.lVerbs.append(instan)
            if instan.helping:
                self.hVerbs.append(instan)
    def sortItAllOut(self):
        """sorts everything"""
        self.lVerbs.sort()
        self.hVerbs.sort()
        self.verbs.sort()
        self.nouns.sort()
        self.adjs.sort()
        self.advs.sort()
        self.conjs.sort()
        self.preps.sort()
        self.interjs.sort()
        self.masterVerbs.sort()
        self.words=self.nouns[:]+self.masterVerbs[:]+self.adjs[:]+self.advs[:]+self.conjs[:]+self.preps[:]+self.interjs[:]
        return self.words
    def save(self, filename, words=None):
        """should not contain be, have"""
        if words is None:
            words=self.sortItAllOut()

        doc=minidom.Document()
        main=doc.createElement("words")
        doc.appendChild(main)

        for word in words:
            word.toXML(doc)

        f=open(filename, "w")
        f.write(doc.toprettyxml())
        f.close()
