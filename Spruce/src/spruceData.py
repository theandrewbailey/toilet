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

# preset words
'''
conjsubtime=("after","before","since","until","when","while")
conjsubcause=("as","because","in orderThat","now that","since","so")
conjsuboppose=("although","even though","though","whereas","while")
conjsubcondition=("even if","if","in case","in case that","only if","unless","whether or not")
conjrel=("both/and","either/or","neither/nor","not only/but also","whether/or")
conj=("and","but","for","nor","or","so","yet")
noun=('August','Spartan','acid','amp','ant','arch','base','bluff','brother','burden','cavalier','chipper','coward','cross',
      'curmudgeon','drol','expert','feather','game','grandfather','heaven','leisure','limp','manner','matron','minute',
      'mother','part','precipitate','purpose','rank','routine','slack','sound','spruce','stimulus','ward')
adj=('aberrant','abrasive','abrupt','absolute','abstract','abstruse','absurd','accurate','acrid','acute','adept','adequate',
     'adroit','adverse','affectionate','aghast','agile','alert','aloof','amateur','ample','angular','apposite','appropriate',
     'approximate','apt','arcane','arid','articulate','asinine','astute','august','austere','avid','awesome','awestruck',
     'awkward','backward','bad','bald','bare','basic','bellicose','benign','berserk','bitter','bittersweet','bizarre','bland',
     'blank','bleak','blind','blithe','blunt','bold','bovine','brash','brave','brazen','brief','bright','brisk','brittle',
     'broad','broken','brusque','callous','callow','calm','candid','celibate','certain','chaste','circular','circumspect',
     'civil','clandestine','clean','clear','clever','coarse','cold','comatose','common','compact','compassionate','complete',
     'complex','compliant','concise','conjoint','connate','considerate','consulate','consummate','contrite','converse','cool',
     'correct','covert','coy','crass','craven','crestfallen','crisp','crude','cruel','cumbersome','curt','cute','daft','damp',
     'dapper','dark','deaf','dear','debonair','deep','definite','deft','deliberate','delicate','demure','dense','desolate',
     'desperate','devout','difficult','dim','dire','direct','discreet','discreet','discriminate','distinct','distinct',
     'distraught','divine','docile','dour','downcast','downtrodden','downward','drunken','dry','due','dulcet','dumb','eager',
     'earnest','effeminate','effete','elaborate','entire','erect','erudite','even','evil','exact','explicit','express',
     'exquisite','extortionate','facile','faint','fair','false','familiar','fearsome','feeble','feminine','fervid','fetid',
     'fickle','fierce','fine','firm','flat','florid','fluid','fond','foreign','forlorn','forsaken','forthright','fortunate',
     'forward','foul','fragile','frail','frank','free','fresh','frigid','frolicsome','frozen','full','fulsome','futile',
     'geneel','genuine','germane','ghastly','glad','glum','grand','grandiose','grave','great','grim','gross','grotesque',
     'gruesome','gruff','haggard','handsome','haphazard','hard','harsh','heartbroken','heartfelt','heavenly','hidden','high',
     'hoarse','hollow','honest','horrid','hostile','hot','huge','human','humane','humble','hurt','immaculate','immature',
     'immediate','immense','immoderate','immodest','imperfect','implicit','impolite','importunate','imprecise','improper',
     'impure','inane','inept','insipid','intimate','intrepid','intricate','irate','irksome','irregular','irresolute','jejune',
     'jocose','jocular','joint','just','keen','kind','lachrymose','lackluster','lame','languid','lax','leaden','level','lewd',
     'light','limber','limpid','lissome','lithe','lonesome','loose','lost','loud','low','lucid','lush','macabre','mad','maiden',
     'masculine','mature','mean','meddlesome','meek','mellow','mere','mild','minuscule','minute','mistaken','moderate','modest',
     'moist','morbid','morose','mundane','muscular','mute','narrow','near','neat','nice','niggard','nimble','noisome','nude',
     'numb','obdurate','oblique','obscene','obscure','obstinate','obtuse','odd','onward','open','operose','opportune','opposite',
     'oracular','ordinate','ornate','orthodox','outspoken','outward','overt','paranoid','particular','passionate','peculiar',
     'pellucid','penultimate','peptic','perfect','perpendicular','pert','perverse','picturesque','placid','plain','polite',
     'poor','pristine','private','profane','profligate','profound','profuse','prompt','proper','proportionate','proud','puerile',
     'pure','putrid','quaint','quarrelsome','queer','quick','quiet','rabid','rancid','random','rank','rapid','rapt','rare',
     'rash','regular','remote','resolute','ribald','rich','rigid','ripe','risque','robust','rotund','rough','round','rude',
     'sad','safe','sage','sane','saturnine','savage','scarce','secret','secure','sedate','separate','serene','serpentine',
     'servile','severe','shaken','shallow','sharp','short','shrewd','shrill','shy','sick','similar','simple','sincere',
     'singular','sinister','sleek','slender','slick','slight','sloven','slow','sly','smart','smooth','smug','snide','snug',
     'sober','sodden','soft','sole','solemn','solid','solute','somber','sordid','sore','sound','sour','sparse','spartan',
     'splendid','sprite','spry','square','staid','stale','stalwart','stark','state','statuesque','staunch','steadfast','steep',
     'stern','stiff','stolid','stout','straight','straightforward','strange','strict','strong','stubborn','stupid','suave',
     'subordinate','subtle','succinct','sudden','sullen','superb','supple','sure','svelte','sweet','swift','swollen','tacit',
     'taciturn','tactile','tame','tart','taut','temperate','tender','tense','tepid','terse','thick','thin','thorough','thus',
     'tight','timid','tiresome','torn','torpid','torrid','tough','tranquil','triangular','trite','true','tumid','turgid',
     'ultimate','unctuous','uniform','unique','upright','upset','upward','urbane','utile','vague','vain','valid','vapid',
     'vast','venturesome','verbose','vile','vivid','volatile','vulgar','wanton','warm','wayward','weak','wearisome','weird',
     'wet','whole','wide','wild','winsome','wise','wooden','wrong','wry')
color=('amber','amethyst','aquamarine','azure','black','blue','brown','burgundy','cerulean','cobalt blue','coral','crimson',
       'cyan','ebony','emerald','gold','gray','green','indigo','lavender','lilac','lime','livid','lurid','magenta','maroon',
       'mauve','monochrome','navy blue','ochre','olive','orange','pale','pallid','pink','plum','puce','purple','red','rose',
       'sangria','sanguine','sapphire','scarlet','silver','teal','turquoise','violet','viridian','white','yellow')
prep=("aboard","about","above","absent","according to","across","after","against","along","alongside","amid","amidst","among",
      "amongst","around","as","as far as","as to","as well as","aside from","aslant","astride","at","athwart","atop","barring",
      "because of","before","behind","below","beneath","beside","besides","between","beyond","but","by","by means of","close to",
      "despite","down","due to","during","except","except for","failing","far from","following","for","from","in",
      "in accordance with","in addition to","in case of","in front of","in lieu of","in place of","in spite of","inside",
      "inside of","instead of","into","like","minus","near","near to","next","next to","notwithstanding","of","off","on",
      "on account of","on behalf of","on top of","onto","opposite","out","out from","out of","outside","outside of","over",
      "owing to","past","per","plus","prior to","pursuant to","regarding","regardless of","round","save","since","subsequent to",
      "than","through","throughout","till","times","to","toward","towards","under","underneath","unlike","until","up","upon",
      "via","with","within","without","worth")
adv=("about","acapella","after","afterwards","again","agape","almost","alone","aloud","already","also","amuck","anew","anyway",
     "apart","arm-in-arm","around","aside","askance","askew","asunder","away","awhile","back","back-and-forth","back-to-back",
     "backward","backwards","beforehand","bent","better","bird-like","both-ways","businesslike","cat-like","clearer","clockwise",
     "closer","counter-clockwise","deadpan","deeper","downward","elsewhere","ever-so-slightly","everywhere",
     "face-first","faster","ghastly","gingerly","less","most","outright",
     "feet-first","first","forth","forward","forward-and-back","further","furthermore","halfway","hand-in-hand","hard","harder",
     "head-first","head-to-toe","headlong","higher","however","instead","inward","inwards","just","just-so","ladylike","left",
     "left-and-right","left-to-right","leftward","lengthwise","less","likewise","lots","louder","lower","meanwhile","more","nearer",
     "next","non-stop","nonetheless","now","now-and-then","off","off-key","off-tune","often","once","only","onward",
     "onwards","otherwise","out","outward","outwards","overhead","overmuch","paler","parallel","partway","past","pointblank",
     "quicker","regardless","right","right-and-left","right-to-left","rightside-up","rightward","side-by-side","side-to-side",
     "sideways","skyward","slower","soaked","softer","somehow","sometimes","somewhat","soon","still","straight",
     "stupified","then","thrice","tighter","to-and-fro","together","too","twice","unannounced","unawares","underfoot","unseen",
     "up-and-down","upbeat","upright","upside-down","upward","verbatim","very","withdrawn","worrisome")
hVerb=('can','could','do','have','may','might','must','shall','should','will','would')
lVerb=('appear','become','do','feel','get','go','grow','have','look','prove','remain','seem','smell','sound','taste','turn')
iVerb=('answer','ask','asphyxiate','backlog','belt','benefit','break','carry','catch','click','collide',
    'cook','correspond','cry','dance','deliver','detonate','digress','diminish','distribute','drill',
    'drink','drop','eat','excel','expatriate','fail','fall','flatten','flatter','flourish','foan',
    'fraternize','fulminate','gorge','hunker','immigrate','impose','inch','inimite','irritate','join',
    'lactate','languish','lock','loop','lord','menstruate','mouse','mush','oblige','orientate','pant',
    'phone','piss','play','pose','powdered','publish','read','recite','regenerate','reign','relate',
    'resolve','rev','rot','run','scamper','secede','shimmy','shop','shoplift','shudder','shy','sing',
    'sizzle','ski','skip','smile','snap','snore','sock','soften','somnambulate','stab','steam','stifle',
    'stock','strangle','surfiet','swing','transgress','trek','troupe','volley','waddle','wait','walk',
    'wash','win','wince','wither','write','yawn','yearn','zoom')
verb=('abate','abstain','bother','close','convolute','coordinate','double','frolic','frustrate','gain','ginger','importune',
      'order','prim','rot','spruce','trim','utter','ward','suffice','will')
'''

# some old shit
'''
for word in conj:
    c=Conjunction(word,CONJ|COORD)
    conjs.append(c)
for word in conjsubtime:
    c=Conjunction(word,CONJ|SUBTIME)
    conjs.append(c)
for word in conjsubcause:
    c=Conjunction(word,CONJ|SUBCAUSE)
    conjs.append(c)
for word in conjsuboppose:
    c=Conjunction(word,CONJ|SUBOPPO)
    conjs.append(c)
for word in conjsubcondition:
    c=Conjunction(word,CONJ|SUBCOND)
    conjs.append(c)
for word in conjrel:
    c=Conjunction(word,CONJ|COREL|COORD)
    words=word.split("/")
    c.name=words[1]
    c.relative=words[0]
    conjs.append(c)
conjs.sort()

for word in noun:
    nouns.append(Noun(word,NOUN))
nouns.sort()

for word in adj:
    adjs.append(Adjective(word,ADJ))
colors=[]
for word in color:
    adjs.append(Adjective(word,ADJ|COLOR))
adjs.sort()

for word in prep:
    preps.append(Preposition(word,PREP))
preps.sort()

for word in adv:
    advs.append(Adverb(word,ADV))
advs.sort()

for word in verb:
    verbs.append(Verb(word,VERB | INFINITIVE))
for word in hVerb:
    v=Verb(word,VERB | HELP)
    verbs.append(v)
lVerbs=[]
for word in lVerb:
    v=Verb(word,VERB | INFINITIVE | LINK)
    if word=='be': pass
    verbs.append(v)
for word in iVerb:
    v=Verb(word,VERB | INFINITIVE | INTRANS)
    verbs.append(v)
verbs.sort()

doc=minidom.Document()
main=doc.createElement("words")
doc.appendChild(main)
for word in conjs:
    word.toXML(doc)
for word in nouns:
    word.toXML(doc)
for word in adjs:
    word.toXML(doc)
for word in preps:
    word.toXML(doc)
for word in advs:
    word.toXML(doc)
for word in verbs:
    word.toXML(doc)

for x in main.childNodes:
    print x.nodeName

#print doc.toprettyxml()
f=open("dictionary.xml","w")
f.write(doc.toprettyxml())
#doc.writexml(f,'\t','\r\n')
f.close()

verbin=[]
XMLin=minidom.parseString(doc.toprettyxml().replace('\n','').replace('\t',''))
#print XMLin.toprettyxml()
for node in XMLin.documentElement.getElementsByTagName("verb"):
    v=Verb("",0)
    v.fromXML(node)
    verbin.append(v)
#    print v
for node in XMLin.documentElement.getElementsByTagName("conj"):
    c=Conjunction("",0)
    c.fromXML(node)
    print c

'''
