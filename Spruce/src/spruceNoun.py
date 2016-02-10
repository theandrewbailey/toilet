# -*- coding: utf-8 -*-
# Leisure is a limp spruce! by praetor_alpha
# parts of speech module
# noun support

from spruceParts import *

class Noun(Word):
    abbrev="noun"

    def __init__(self,e):
        self._an=None
        self.gender=0
        self.description=0
        self.reset()
        Word.__init__(self, e)

        if type(e)==str or type(e)==unicode: return

        if 'M' in self.attribs:
            self.gender=MASCULINE
        if 'F' in self.attribs:
            self.gender+=FEMININE
        if 'm' in self.attribs:
            self.mass=True
        if 'a' in self.attribs:
            self._an=True
        elif 'A' in self.attribs:
            self._an=False
        if 'P' in self.attribs:
            self.plural=False

    def toXML(self,doc):
        e=Word.toXML(self, doc)
        attribs=""
        if self.gender & MASCULINE:
            attribs+='M'
        if self.gender & FEMININE:
            attribs+='F'
        if self.mass:
            attribs+='m'
        if self._an==True:
            attribs+='a'
        elif self._an==False:
            attribs+='A'
        if self.plural==False:
            attribs+='P'
        if attribs!="":
            e.setAttribute("attrib", attribs)
        return e

    def reset(self):
        self.pluralize=False
        self.specificity=None
        self.mass=False
        self.adjs=[]

    def getPlural(self):
        if self.plural != False:
            return Word.getPlural(self)
        return self.name

    def addAdj(self, adjList):
        if choice(bools):
            self.adjs+=[choice(adjList)]

    def write(self):
        out=""
        if self.specificity:
            out+="the "
        elif self.specificity==False and not self.mass and not self.pluralize:
            if self.an:
                out+="an "
            else:
                out+="a "
        for adj in self.adjs:
            out += adj.write()
            out += ' '
        if self.pluralize:
            out+=self.plural
        else:
            out+=self.name
        return out

    def getAn(self):
        var=self._an
        comp=self.name
        if len(self.adjs) > 0:
            var=self.adjs[0].an
            comp=self.adjs[0].name
        if var != None:
            return var

        if comp[0] in vowel or (comp[0] in 'AEFHILMNORSX' and comp[1].isupper()):
            return True
        return False
    an=property(getAn)

class Adjective(Noun):
    abbrev="adj"

    def __init__(self,e):
        self.degree=0
        self.color=False
        self.number=False
        self.deg1=None
        self.deg2=None
        Noun.__init__(self, e)

        if type(e)==str or type(e)==unicode: return

        # old skool
        if e.hasAttribute("color"):
            self.color=True
        if e.hasAttribute("number"):
            self.number=True

        # new school
        if 'c' in self.attribs:
            self.color=True
        if 'n' in self.attribs:
            self.number=True
        self.deg1=e.getAttribute("deg1")
        self.deg2=e.getAttribute("deg2")

    def toXML(self,doc):
        e=Noun.toXML(self,doc)
        attribs=e.getAttribute("attrib")
        if self.color:
            attribs+='c'
        if self.number:
            attribs+='n'
        if self.deg1:
            e.setAttribute("deg1", self.deg1)
        if self.deg2:
            e.setAttribute("deg2", self.deg2)
        if attribs!="":
            e.setAttribute("attrib", attribs)
        return e

    def write(self):
        if self.degree == 1:
            if self.deg1 != None:
                return self.deg1
            elif self.syl == 1:
                return self.name + "er"
            else:
                return "more " + self.name
        elif self.degree == 2:
            if self.deg2 != None:
                return self.deg2
            elif self.syl == 1:
                return self.name + "est"
            else:
                return "most " + self.name
        return self.name

class You(Noun):
    syl=1
    name="(You)"
    pluralize=True
    def write(self):return ""
    def toXML(self,doc):pass
    def __init__(self): pass

class Pronoun(Noun):
    abbrev="pro"
    target=None
    def __init__(self,e):
        Noun.__init__(self,e)
        self.target=None

class Adverb(Adjective):
    """uses Adjective's stuff (lo! the beauty of inheritance!)"""
    abbrev="adv"
