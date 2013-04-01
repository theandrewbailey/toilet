# -*- coding: utf-8 -*-
# Leisure is a limp spruce! by praetor_alpha
# parts of speech module
# verb support

from spruceParts import vowel
from spruceParts import *

availableTenses=(
    PRESENT,
    FUTURE,
    PAST,
    PRESENT,
    FUTURE,
    PAST,
    PRESENT,
    FUTURE,
    PAST,
    PRESENT | PROGRESSIVE,
    FUTURE | PROGRESSIVE,
    PAST | PROGRESSIVE,
    PRESENT | PROGRESSIVE,
    FUTURE | PROGRESSIVE,
    PAST | PROGRESSIVE,
    PRESENT | PERFECT,
    FUTURE | PERFECT,
    PAST | PERFECT,
    PRESENT | PERFECT | PROGRESSIVE,
    FUTURE | PERFECT | PROGRESSIVE,
    PAST | PERFECT | PROGRESSIVE,
    )

class Verb(Word):
    abbrev="verb"

    def __init__(self,e):
        self._part=None
        self._past=None
        self._pastPart=None
        self.intransitive=False
        self.linking=False
        self.linkToNoun=False
        self.linkToAdj=False
        self.helping=False
        self.infinitive=False
        self.tense=PRESENT
        self.mood=None
        self.old=False
        self.adverb=None
        Word.__init__(self, e)

        if type(e)==str or type(e)==unicode: return

        # old skool
        if e.hasAttribute("help"):
            self.helping=True
        if e.hasAttribute("infin"):
            self.infinitive=True
        if e.hasAttribute("link"):
            self.linking=True
        if e.hasAttribute("intrans"):
            self.intransitive=True

        # new school
        if e.getAttribute("part")!="":
            self._part=e.getAttribute("part")
        if e.getAttribute("past")!="":
            self._past=e.getAttribute("past")
        if e.getAttribute("pastPart")!="":
            self._pastPart=e.getAttribute("pastPart")
        if 'i' in self.attribs:
            self.intransitive=True
        if 'h' in self.attribs:
            self.helping=True
        if 'f' in self.attribs:
            self.infinitive=True
        if 'l' in self.attribs:
            self.linking=True
            if 'a' in self.attribs:
                self.linkToAdj=True
            if 'n' in self.attribs:
                self.linkToNoun=True

    def toXML(self,doc):
        e=Word.toXML(self,doc)
        attribs="";
        if self.intransitive:
            attribs+="i"
        if self.infinitive:
            attribs+="f"
        if self.helping:
            attribs+="h"
        if self.linking:
            attribs+="l"
            if self.linkToAdj:
                attribs+="a"
            if self.linkToNoun:
                attribs+="n"
        if self._part:
            e.setAttribute("part", self._part)
        if self._past:
            e.setAttribute("past", self._past)
        if self._pastPart:
            e.setAttribute("pastPart", self._pastPart)
        if attribs!="":
            e.setAttribute("attrib", attribs)
        return e

    def getPast(self):
        if self._past:
            return self._past
        elif self.name[-1] == 'e':
            return self.name + 'd'
        #elif self.name[-1] not in vowel and self.name[-2] in vowel and determineSyllables(self.name)==1:
        #    return self.name + self.name[-1]+"ed"
        return self.name + "ed"
    past=property(getPast)

    def getPastPart(self):
        if self._pastPart:
            return self._pastPart
        elif self.name[-1] == 'e':
            return self.name + 'd'
        #elif self.name[-1] not in vowel and self.name[-2] in vowel and determineSyllables(self.name)==1:
        #    return self.name + self.name[-1]+"ed"
        return self.name + "ed"
    pastPart=property(getPastPart)

    def getPart(self):
        if self._part:
            return self._part
        if self.name[-1] == 'e' and self.name[-2] != 'e':
            return self.name[:-1] + "ing"
        #elif self.name[-1] not in vowel and self.name[-2] in vowel:
        #    return self.name + self.name[-1] + "ing"
        return self.name + "ing"
    part=property(getPart)

    def addAdv(self,advList):
        if choice(bools):
            self.adverb=choice(advList)

    def write(self, subject=None, tense=None):
        outAdv=False
        if tense==None:
            tense=self.tense
            outAdv=True
        out=""

        if tense & (PERFECT | PROGRESSIVE) == (PERFECT | PROGRESSIVE):
            out += vHave.write(subject, tense & ~(PERFECT | PROGRESSIVE)) + ' been ' + self.part
        elif tense & PERFECT:
            out += vHave.write(subject, tense ^ PERFECT) + ' ' + self.pastPart
        elif tense & PROGRESSIVE:
            out += vBe.write(subject, tense ^ PROGRESSIVE) + ' ' + self.part
        elif tense & PRESENT and subject and not subject.pluralize:
            out += self.plural
        elif tense & PAST:
            out += self.past
        elif tense & FUTURE:
            out += "will " + self.name
        else:
            out += self.name

        if outAdv and self.adverb!=None:
            out+=' '
            out+=self.adverb.write()

        return out

class Be(Verb):
    def write(self, subject=None, tense=None):
        if tense==None:
            tense=self.tense
        if tense & (PERFECT | PROGRESSIVE) == (PERFECT | PROGRESSIVE):
            return vHave.write(subject, tense & ~(PERFECT | PROGRESSIVE)) + ' been ' + self.part
        elif tense & PERFECT:
            return vHave.write(subject, tense ^ PERFECT) + ' ' + self.pastPart
        elif tense & PROGRESSIVE:
            return vBe.write(subject, tense ^ PROGRESSIVE) + ' ' + self.part
        elif tense & PRESENT:
            if subject and subject.name=='I':
                return self.sing1
            elif not subject.pluralize:
                return self.sing3
            return self.sing2
        elif tense & PAST:
            if subject and subject.pluralize:
                return self.plurpast
            return self.singpast
        elif tense & FUTURE:
            return "will "+self.name
        return self.name

    def toXML(self,doc): return
    def __init__(self):
        Verb.__init__(self, "be")
        self.sing1="am"
        self.sing2="are"
        self.sing3="is"
        self.singpast="was"
        self.plurpast="were"
        self._part="been"
        self.infinitive=True
        self.helping=True
        self.link=True

class Have(Verb):
    def __init__(self):
        Verb.__init__(self, "have")
        self._plural="has"
        self._past="had"

vBe=Be()
vHave=Have()