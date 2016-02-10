
import spruceData
import Editor.UI.ConjunctionUI as ConjunctionUI
from Editor.AbstractModel import abstractModel
from PyQt4 import QtCore, QtGui

class ConjModel(ConjunctionUI.Ui_Form,abstractModel):
    def setupUi(self):
        ConjunctionUI.Ui_Form.setupUi(self, self)
        abstractModel.setupUi(self)
        QtCore.QObject.connect(self.tConj, QtCore.SIGNAL("textChanged(QString)"), self.process)
        QtCore.QObject.connect(self.tRel, QtCore.SIGNAL("textChanged(QString)"), self.processRel)
        self.defText=self.tConj
        self.defSyl=self.conjSyl
        self.defClass=spruceData.Conjunction
    def processRel(self,str):
        self.relSyl.setValue(spruceData.determineSyllables(str.__str__()))
    def changed(self,int):
        if abstractModel.changed(self,int) is not None:
            if self.selected.rel is None:
                self.tRel.setText("")
            else:
                self.tRel.setText(self.selected.rel)

            if self.selected.subordinate:
                self.rSub.setChecked(True)
            else:
                self.rCoord.setChecked(True)

    def populate(self, dic):
        abstractModel.populate(self, {})
        self.dic=dict
        for word in dic.conjs:
            if word.rel is not None:
                key= word.rel + '/' + word.name
            else:
                key= word.name
            self.model[key]=word
            self.items[key]=QtGui.QListWidgetItem(key, self.lWords)

#    def process(self,str):
#        """Fired when the text in the first text box is changed, populates syllables and looks in list for word"""
#        str=str.__str__()
#        if str == "":
#            self.defSyl.setValue(0);
#            self.selected=None
#            return None
#        if self.model.has_key(str):
#            self.selected=self.model[str]
#            word=self.selected
#        else:
#            self.selected=None
#            word=self.defClass(str)
#        self.defSyl.setValue(spruceData.determineSyllables(str))
#        return word
