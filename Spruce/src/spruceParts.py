# -*- coding: utf-8 -*-
# Leisure is a limp spruce! by praetor_alpha
# parts of speech module

from xml.dom import minidom
from xml.dom import Node
from random import choice

MASCULINE=1<<1
FEMININE=1<<2

PROGRESSIVE=1<<0
PERFECT=1<<1
PRESENT=1<<2
PAST=1<<3
FUTURE=1<<4
INFINITIVE=1<<6     # can be the only verb in the sentence (or the only word!)
HELP=1<<7           # can support another verb
LINK=1<<8           # must have an object, can be an noun or adjective
INTRANS=1<<9        # can go without an object

COREL=1<<14
COORD=1<<15

vowel=('a','e','i','o','u')
bools=(False,True)
threeWay=(True,False,None)
compareBySyllable=False

def determineSyllables(text):
    lastWasVowel=False
    count=0
    for x in text:
        if x.lower() in vowel and not lastWasVowel:
            count += 1
            lastWasVowel=True
        else:
            lastWasVowel=False
    try:
        if text[-1] == 'e' and count > 1:
            count -= 1
        elif text[-1]=='o' and text[-2] in vowel:
            count += 1
        if count == 0:
            count = 1
    except:
        pass
    return count

class Word(object):
    abbrev="unknown"

    def __init__(self,e):
        """pass in an XML node, and unmarshals the word to an object"""
        self._syl=None
        self._plural=None
        self._pSyl=None
        self.name=None
        self.pluralize=False
        self.attribs=""

        # a word can have different forms (power, to power, powerful, powerfully, etc...)
        # None = don't do it || object representing word
        self.noun=None       # -dom -ity -ment -sion -ation -ion -ness -ance -ence -er -or -ist
        self.verb=None       # -en -ize -ate -ify -fy
        self.adj=None        # -ive -en -ic -al -able -y -ous -ful -less
        self.adv=None        # -ly
        self.who=None
        self.thing=None
        self.derived=False   # not the base word, ie. one of those

        if type(e)==str or type(e)==unicode:
            self.name=e.strip()
            return

        #self.name=e.lastChild.nodeValue.strip()
        self.name=e.getAttribute("word")
        x=self.syl
        if e.getAttribute("attrib")!="":
            self.attribs=e.getAttribute("attrib")
        if e.getAttribute("plural")!="":
            self.plural=e.getAttribute("plural")
        if e.getAttribute("syl")!="":
            self.syl=int(e.getAttribute("syl"))
        if e.getAttribute("pSyl")!="":
            self.pSyl=int(e.getAttribute("pSyl"))
        for el in e.childNodes:
            # child nodes, for related words, to be determined
            if el.nodeName=="noun":
                pass
            elif el.nodeName=="verb":
                pass
            elif el.nodeName=="adj":
                pass
            elif el.nodeName=="adv":
                pass
            elif el.nodeName=="who":
                pass
            elif el.nodeName=="thing":
                pass

    def toXML(self,doc):
        """pass in the document object of an XML, and marshals the word to an XML node"""
        e=doc.createElement(self.abbrev)
        doc.documentElement.appendChild(e)
        e.setAttribute("word", self.name)
        if self._plural:
            e.setAttribute("plural", self._plural)
        if self._syl:
            e.setAttribute("syl", self._syl)
        if self._pSyl:
            e.setAttribute("pSyl", self._pSyl)

        # to be determined
        if self.noun:
            pass
        if self.verb:
            pass
        if self.adj:
            pass
        if self.adv:
            pass
        if self.who:
            pass
        if self.thing:
            pass
        return e

    def getSyl(self):
        if self.pluralize and self._pSyl:
            return self._pSyl
        if not self.pluralize and self._syl:
            return self._syl
        return determineSyllables(self.write())
    def setSyl(self,v):
        self._syl=v
    syl=property(getSyl, setSyl)

    def getPSyl(self):
        return self._pSyl
    def setPSyl(self, v):
        self._pSyl=v
    pSyl=property(getPSyl, setPSyl)

    def getPlural(self):
        if self._plural:
            return self._plural
        elif self.name[-1] == 'o':
            if self.name[-2] in vowel:
                return self.name+'s'
            return self.name+'es'
        elif self.name[-1] in ('s','x') or self.name[-2:] in ('sh','ch'):
            return self.name+'es'
        elif self.name[-1] == 'y':
            if self.name.isupper() or self.name[-2] in vowel:
                return self.name+'s'
            return self.name[:-1]+'ies'
        return self.name+'s'
    def setPlural(self, v):
        self._plural=v
    plural=property(getPlural, setPlural)

    def __repr__(self): return '('+self.abbrev+') '+self.name

    def write(self):
        """function that is called to print a sentence"""
        return self.name

    def __cmp__(self, other):
        """alphabetizes, or syllablizes according to self.compareBySyllable"""
        try:
            if compareBySyllable:
                if self.syl==0:
                    self.determineSyllables()
                if other.syl==0:
                    other.determineSyllables()
                if self.syl>other.syl: return 1
                elif self.syl<other.syl: return -1

            # equal syl
            if self.name>other.name: return 1
            elif self.name<other.name: return -1

        except AttributeError: return 1
        return 0

    def __eq__(self, other):
        try:
            if other.name == self.name: return 1
            else: return 0
        except AttributeError: return 0
        return 0

    def __ne__(self, other):
        return not self.__eq__(other)

class Conjunction(Word):
    abbrev="conj"

    def __init__(self,e):
        self.rel=None
        self.subordinate=False
        Word.__init__(self, e)

        if type(e)==str or type(e)==unicode: return

        # old skool
        if e.getAttribute("kind")!="" and int(e.getAttribute("kind")) & (COREL | COORD):
            self.subordinate=True

        # new school
        if e.hasAttribute("rel"):
            self.rel=e.getAttribute("rel")
        if 's' in self.attribs:
            self.subordinate=True

    def toXML(self,doc):
        e=Word.toXML(self,doc)
        if self.rel != None:
            e.setAttribute("rel",self.rel)

        if self.subordinate:
            attribs="s"
            e.setAttribute("attrib", attribs)

        return e

    def write(self):
        """Should not be preceded by a space!"""
        out=""
        if self.name=="however":
            out+="; "
        elif self.subordinate:
            out+=", "
        else:
            out+=' '

        out+=self.name

        if self.name=="however":
            out+=","
        return out

    def __repr__(self):
        if self.rel != None:
            return '('+self.abbrev+') '+self.rel+'/'+self.name
        return Word.__repr__(self)

class Preposition(Word):
    abbrev="prep"

    def __init__(self, e):
        self.target=None
        Word.__init__(self,e)

    def write(self):
        out=self.name
        out+=' '
        if self.target:
            out+=self.target.write()
        return out

class Interjection(Word):
    abbrev="interj"

    def __init__(self, e):
        self.strong=True
        Word.__init__(self, e)

    def write(self):
        if self.strong:
            return self.name[0].capitalize()+self.name[1:]+"! "
        return self.name

