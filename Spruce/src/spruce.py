# -*- coding: utf-8 -*-
# Leisure is a limp spruce! by praetor_alpha

from spruceData import *
from random import choice, randrange
from sys import argv, exit
import os
import spruceParts

dic=None

class Clause(object):
    excite=False
    verb=None
    subject=None
    ido=None
    obj=None
    objcomp=None
    next=None
    interj=None
    multipart=None      # last part of a multi clause sentence
    text=""
    
    def __init__(self,compound=False,v=None,s=None,multi=None,tense=None):
        self.multipart=multi
        if not multi and not randrange(20):
            self.excite = True
            if not randrange(2):
                self.interj=choice(dic.interjs)

        if compound and choice(bools) and choice(bools):
            self.next=choice(dic.conjs)
            self.multipart=False
        
        if v!=None and s!=None:
            self.verb=v
            self.subject=s
        else:
            self.verb=choice(dic.verbs)#.getWord()
            self.verb.addAdv(dic.advs)

            if self.multipart != None:
                self.subject=choice(dic.nouns)#.getWord()
            else:
                self.subject=choice(dic.subjects)#.getWord()

            if not isinstance(self.subject,You):
                if not tense:
                    self.verb.tense=choice(availableTenses)
                else:
                    self.verb.tense=tense
                if self.multipart==False:
                    self.next.next=Clause(False,multi=True,tense=self.verb.tense)
                self.subject.pluralize=choice(bools)
                self.subject.specificity=choice(threeWay)
                self.subject.addAdj(dic.adjs)

            if self.verb.linking:
                if not self.verb.intransitive or choice(bools):
                    self.obj=choice(dic.adjs)#.getWord()
                    '''n=choice(dic.nouns)
                    a=choice(dic.adjs)
                    c.obj=choice((n,a))
                    if c.obj.function == NOUN and choice(bools):
                        p=choice(dic.preps)
                        p.target=n
                        c.objcomp=p
                        n=choice(dic.nouns)
                        a=choice(dic.adjs)
                        c.obj=choice((n,a))'''
            elif not self.verb.intransitive:
                self.obj=choice(dic.nouns)#.getWord()
                self.obj.addAdj(dic.adjs)
                '''if choice(bools) and choice(bools):
                    self.ido=choice(dic.nouns).getWord()
                    self.ido.specificity=choice(threeWay)'''

    def clean(self):
        self.verb.adverb=None
        self.subject.reset()
        if self.obj != None:
            self.obj.reset()

    def write(self): return self.__str__()

    def __str__(self):
        if self.text=="":
            sentence=""
            sentence+=self.subject.write()
            if not isinstance(self.subject,You):
                sentence+=' '
            sentence+=self.verb.write(self.subject)
            if self.ido != None:
                sentence += ' '
                sentence += self.ido.write()
                self.ido.reset()
            if self.obj != None:
                sentence += ' '
                sentence += self.obj.write()
                self.obj.reset()
            if self.objcomp != None:
                sentence += ' '
                sentence += self.objcomp.write()
                self.objcomp.reset()
            self.subject.reset()
            if self.next!=None:
                sentence += self.next.write()
                sentence += ' '
                sentence += self.next.next.write()
            if not self.multipart:
                sentence=sentence[0].capitalize()+sentence[1:]
                if self.interj != None:
                    front=self.interj.write()
                    sentence=front[0].capitalize()+front[1:]+sentence
                if self.excite:
                    sentence += '!'
                else:
                    sentence+='.'
            self.text=sentence
        return self.text

    def getSyllables(self):
        toat=0
        syl=0

        syl=self.subject.determineSyllables()
        #print syl, self.subject.write()
        toat+=syl

        syl=self.verb.determineSyllables(self.subject)
        #print syl, self.verb.write(self.subject)
        toat+=syl
        if self.obj != None:
            syl=self.obj.determineSyllables()
            #print syl, self.obj.write()
            toat+=syl
        if self.objcomp != None:
            syl=self.objcomp.determineSyllables()
            #print syl, self.objcomp.write()
            toat+=syl
        if self.next!=None:
            syl=self.next.determineSyllables()
            #print syl,self.next.write()
            toat+=syl
            syl=self.next.next.getSyllables()
            toat+=syl
        return toat

class Haiku(Clause):
    def determineSyllablesSentence(self,c):
        lastWasVowel=False
        totalcount=0
        count=0
        for x in c.name:
            if x.lower() in vowel and not lastWasVowel:
                count += 1
                lastWasVowel=True
            else:
                lastWasVowel=False
                if self.name[-1] == 'e' and count > 1:
                    count -= 1
                if count == 0:
                    count = 1
                    if c.name == " ":
                        totalcount = totalcount + count
                        count=0
        c.syllables=totalcount

    def __str__(self):
        if self.text=="":
            lineOne=False
            lineTwo=False
            lineThree=False
            
            counter=0
            
            lineOneCount=5
            lineTwoCount=7
            lineThreeCount=5
            
            lineOneSen=""
            lineTwoSen=""
            lineThreeSen=""
            
            while not (lineOne & lineTwo & lineThree):
                v=choice(dic.verbs).getWord()
                v.addAdv(dic.advs)

                c=Clause(v,choice(dic.nouns).getWord())
                c.subject.pluralize=choice(bools)
                c.subject.specificity=choice(threeWay)
                c.subject.addAdj(dic.adjs)

                if c.verb.linking:
                    if not c.verb.intransitive or choice(bools):
                        c.obj=choice(dic.adjs).getWord()
                elif not c.verb.intransitive:
                    c.obj=choice(dic.nouns).getWord()
                    c.obj.addAdj(dic.adjs)
                    c.obj.determineSyllables()

                    if choice(bools) and choice(bools):
                        c.ido=choice(dic.nouns).getWord()
                        c.ido.specificity=choice(threeWay)

                if (c.getSyllables() == lineOneCount) and not lineOne:
                    lineOneSen=c
                    lineOne=True
                elif c.getSyllables() == lineTwoCount and not lineTwo:
                    lineTwoSen=c
                    lineTwo=True
                elif c.getSyllables() == lineThreeCount and not lineThree:
                    lineThreeSen=c
                    lineThree=True
                c.subject.reset()
            self.text="\n------------------------\n"+lineOneSen.__str__()+"\n"+lineTwoSen.__str__()+"\n"+lineThreeSen.__str__()+"\n------------------------\n"
        return self.text

def loadDic(filename):
    """Load the dictionary, and what to do instead of panicing"""
    global dic

    try:
        dic=XMLloader(filename)
    except IOError:
        print "dictionary.xml not found at", filename
        #if __name__ == "__main__":
        print str(spruceParts)
        print str(spruceParts).split()[3]
        print str(spruceParts).split("<module 'spruceParts' from '")[1][:-2]
        exit(1)

def loadDicStr(string):
    """Load the dictionary as a string"""
    global dic
    dic=XMLloader(string, isFile=False)


def talk(text):
    """On Windows, with commandline arg set, speaks, and blocks until the line is complete"""
    if speak==True:
        __speak__.Speak(text)
        __speak__.WaitUntilDone(len(text))

def doHaiku():
    """returns a haiku as a string"""
    h=Haiku()
    return h.__str__()

def doClause(c=True):
    """returns a clause as a string"""
    clause=Clause(c)
    #print clause.getSyllables()
    out=clause.__str__()
    clause.clean()
    return out

def randomWord():
    return choice(dic.words).name

if __name__ == "__main__":
    """will execute if the program is directly run"""
    # Figures out what directory this is, even as an executable
    modPath=str(spruceParts).split("<module 'spruceParts' from '")[1][:-2]
    modPath=modPath.split(os.sep+"spruceParts")[0]
    if ".exe" in modPath:
        modPath=modPath.rpartition(os.sep)[0]
    loadDic(os.path.join(modPath,"dictionary.xml"))

    # some global variables to store commandline arguments
    count=10
    allHaikus=False
    speak=None

    # process commandline arguments
    for x in argv:
        try:
            if x[0]=='-':
                tCount=int(x[1:])
                count=tCount
            elif x[0]=='+':
                if x[1]=='H':
                    allHaikus=True
                elif x[1]=='h':
                    allHaikus=False
                elif x[1]=='s' and speak!=None:
                    speak=True
        except: pass

    # import Speech API (Windows XP+ only)
    if speak and os.name=='nt':
        try:
            import win32com.client
            __speak__=win32com.client.Dispatch("SAPI.SpVoice")
            speak=False
        except:
            print "Speech unavailable"

    for x in xrange(count):
        if allHaikus:
            s=doHaiku()
        elif allHaikus==False:
            s=doClause()
        else:
            if choice(bools):
                s=doHaiku()
            else:
                s=doClause()
        print s
        talk(s)
