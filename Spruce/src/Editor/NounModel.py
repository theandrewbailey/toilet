
import spruceData
import Editor.UI.NounUI as NounUI
from Editor.AbstractModel import abstractModel
from PyQt4 import QtCore

class NounModel(NounUI.Ui_Form,abstractModel):
    def setupUi(self):
        NounUI.Ui_Form.setupUi(self, self)
        abstractModel.setupUi(self)
        QtCore.QObject.connect(self.tSingular, QtCore.SIGNAL("textChanged(QString)"), self.singular)
        QtCore.QObject.connect(self.tPlural, QtCore.SIGNAL("textChanged(QString)"), self.plural)
        QtCore.QObject.connect(self.cPlural, QtCore.SIGNAL("toggled(bool)"), self.noPlural)
        QtCore.QObject.connect(self.bRelative, QtCore.SIGNAL("clicked()"), self.openRelative)
        self.defText=self.tSingular
        self.defSyl=self.singularSyl
        self.defClass=spruceData.Noun
    def ladd(self):
        new1=abstractModel.ladd(self)
        if self.cMass.isChecked():
            new1.uncount=True
        if self.cFeminine.isChecked():
            new1.gender=spruceData.FEM
        elif self.cMasculine.isChecked():
            new1.gender=spruceData.MASC
        if self.cPlural.isChecked():
            new1.pluralizable=False
        else:
            if new1.getPlural()!=self.tPlural.text():
                new1.plural=str(self.tPlural.text())
            if spruceData.determineSyllables(new1.getPlural())!=self.pluralSyl.value():
                new1.pSyllables=self.pluralSyl.value()

    def populate(self, dic):
        abstractModel.populate(self, dic.nouns)
    def singular(self, str):
        word=self.process(str)
        if word != None:
            self.tPlural.setText(word.plural)
    def plural(self,str):
        str=str.__str__()
        self.pluralSyl.setValue(spruceData.determineSyllables(str))
    def noPlural(self, bool):
        self.tPlural.setEnabled(not bool)
        self.pluralSyl.setEnabled(not bool)
