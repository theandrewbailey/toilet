
import spruceData
import Editor.UI.VerbUI as VerbUI
from Editor.AbstractModel import abstractModel
from PyQt4 import QtCore

class VerbModel(VerbUI.Ui_Form,abstractModel):
    def setupUi(self):
        VerbUI.Ui_Form.setupUi(self, self)
        abstractModel.setupUi(self)
        QtCore.QObject.connect(self.tPresent, QtCore.SIGNAL("textChanged(QString)"), self.present)
        QtCore.QObject.connect(self.tPart, QtCore.SIGNAL("textChanged(QString)"), self.part)
        QtCore.QObject.connect(self.tPast, QtCore.SIGNAL("textChanged(QString)"), self.past)
        QtCore.QObject.connect(self.tPastpart, QtCore.SIGNAL("textChanged(QString)"), self.pastpart)
        self.defText=self.tPresent
        self.defSyl=self.presentSyl
        self.defClass=spruceData.Verb
    def ladd(self):                     # needs work
        new1=abstractModel.ladd(self)
        if new1.part!=self.tPart.text():
            new1._part=str(self.tPart.text())
        if spruceData.determineSyllables(new1.part)!=self.partSyl.value():
            new1.pastSyl=self.partSyl.value()
        if new1.past!=self.tPast.text():
            new1._past=str(self.tPast.text())
        if spruceData.determineSyllables(new1.past)!=self.pastSyl.value():
            new1.pastSyl=self.pastSyl.value()
        if new1.pastPart!=self.tPastpart.text():
            new1._pastPart=str(self.tPastpart.text())
        if spruceData.determineSyllables(new1.pastPart)!=self.pastpartSyl.value():
            new1.pastPartSyl=self.pastpartSyl.value()

    def populate(self, dic):
        abstractModel.populate(self, dic.verbs)
    def present(self,str):
        word=self.process(str)
        if word==None: return
        self.tPart.setText(word.getPart())
        self.tPast.setText(word.getPast())
        self.tPastpart.setText(word.getPastPart())
    def part(self,str):
        self.partSyl.setValue(spruceData.determineSyllables(str.__str__()))
    def past(self,str):
        self.pastSyl.setValue(spruceData.determineSyllables(str.__str__()))
    def pastpart(self,str):
        self.pastpartSyl.setValue(spruceData.determineSyllables(str.__str__()))
