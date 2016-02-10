import spruceData
from PyQt4 import QtCore, QtGui
from Editor.RelativeModel import RelativeModel


class abstractModel(QtGui.QFrame):
    """Base class for the tabs, does most of the work"""
    def __init__(self):
        QtGui.QFrame.__init__(self)
        self.setupUi()
    def setupUi(self):
        """Sets up common event handlers"""
        QtCore.QObject.connect(self.bAdd, QtCore.SIGNAL("clicked()"), self.ladd)
        QtCore.QObject.connect(self.bRemove, QtCore.SIGNAL("clicked()"), self.lremove)
        QtCore.QObject.connect(self.lWords, QtCore.SIGNAL("currentRowChanged(int)"), self.changed)
    def ladd(self):
        if self.selected:
            new1=self.selected
        else:
            new1=self.defClass(self.defText.text().__str__())
            self.model[new1.name]=new1
            self.items[new1.name]=[QtGui.QListWidgetItem(new1.name,self.lWords)]
        if new1.syl!=self.defSyl.value():
            new1.syllables=self.defSyl.value()
        return new1
    def lremove(self):
        """Fired on the Remove Entry button"""
        item=self.lWords.currentItem()
        if (item == None):
            print("no item")
            return
        self.items[item.text().__str__()]=None
        self.lWords.takeItem(self.lWords.currentRow())
        for key in self.dict[:]:
            if key.name==item.text().__str__():
                self.dict.remove(key)
                break;
    def changed(self, int):
        """Fired when the selection is changed in the list, fills the first box and syllables"""
        if int==-1:
            return None
        self.selected=self.model[self.lWords.currentItem().text().__str__()]
        self.defText.setText(self.selected.name)
        return self.selected
    def populate(self, dict):
        """Clears out all entries in the list, repopulates them with the specified dictionary"""
        for i in xrange(self.lWords.count()-1, -1, -1):
            self.lWords.takeItem(i)
        self.items={}
        self.model={}
        for word in dict:
            self.model[word.name]=word
            self.items[word.name]=QtGui.QListWidgetItem(word.name, self.lWords)
        self.dict=dict
    def process(self, str):
        """Fired when the text in the first text box is changed, populates syllables and looks in list for word"""
        str=str.__str__()
        if str == "":
            self.defSyl.setValue(0);
            self.selected=None
            self.lWords.setCurrentRow(-1)
            return None
        if self.model.has_key(str):
            self.selected=self.model[str]
            word=self.selected
            if self.lWords.currentRow()==-1:
                try:
                    self.lWords.setCurrentItem(self.items[word.name][0])
                except:
                    self.lWords.setCurrentItem(self.items[word.name])
        else:
            self.selected=None
            word=self.defClass(str)
            self.lWords.setCurrentRow(-1)
        self.defSyl.setValue(spruceData.determineSyllables(str))
        return word
    def openRelative(self):
        self.relativeModel=RelativeModel()
        self.relativeModel.setupUi()
