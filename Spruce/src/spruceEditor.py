#!/usr/bin/python
# -*- coding: utf-8 -*-
# Leisure is a limp spruce! by praetor_alpha
# UI editor

from Editor.MassModel import MassModel
import sys
import os
import spruceData
import Editor.UI.SpruceUI as SpruceUI
import Editor.UI.AdjectiveUI as AdjectiveUI
import Editor.UI.AdverbUI as AdverbUI
import Editor.UI.PrepositionUI as PrepositionUI
import Editor.UI.InterjectionUI as InterjectionUI
import spruce as Spruce
from PyQt4 import QtCore, QtGui
from Editor.AbstractModel import abstractModel
from Editor.NounModel import NounModel
from Editor.VerbModel import VerbModel
from Editor.ConjModel import ConjModel

modPath=str(Spruce).split("<module 'spruce' from '")[1][:-2].split(os.sep+"spruce.pyc")[0]
Spruce.loadDic(os.path.join(modPath,"dictionary.xml"))

class AdjModel(AdjectiveUI.Ui_Form, abstractModel):
    def setupUi(self):
        AdjectiveUI.Ui_Form.setupUi(self, self)
        abstractModel.setupUi(self)
        QtCore.QObject.connect(self.tAdj, QtCore.SIGNAL("textChanged(QString)"), self.process)
        self.defText=self.tAdj
        self.defSyl=self.adjSyl
        self.defClass=spruceData.Adjective
    def populate(self, dic):
        abstractModel.populate(self, dic.adjs)

class AdvModel(AdverbUI.Ui_Form, abstractModel):
    def setupUi(self):
        AdverbUI.Ui_Form.setupUi(self, self)
        abstractModel.setupUi(self)
        QtCore.QObject.connect(self.tAdv, QtCore.SIGNAL("textChanged(QString)"), self.process)
        self.defText=self.tAdv
        self.defSyl=self.advSyl
        self.defClass=spruceData.Adverb
    def populate(self, dic):
        abstractModel.populate(self, dic.advs)

class PrepModel(PrepositionUI.Ui_Form, abstractModel):
    def setupUi(self):
        PrepositionUI.Ui_Form.setupUi(self, self)
        abstractModel.setupUi(self)
        QtCore.QObject.connect(self.tPrep, QtCore.SIGNAL("textChanged(QString)"), self.process)
        self.defText=self.tPrep
        self.defSyl=self.prepSyl
        self.defClass=spruceData.Preposition
    def populate(self, dic):
        abstractModel.populate(self, dic.preps)

class InterjModel(InterjectionUI.Ui_Form, abstractModel):
    def setupUi(self):
        InterjectionUI.Ui_Form.setupUi(self, self)
        abstractModel.setupUi(self)
        QtCore.QObject.connect(self.tInterj, QtCore.SIGNAL("textChanged(QString)"), self.process)
        self.defText=self.tInterj
        self.defSyl=self.interjSyl
        self.defClass=spruceData.Interjection
    def populate(self, dic):
        abstractModel.populate(self, dic.interjs)

class MyForm(QtGui.QMainWindow):
    def save(self):
        global dic
        dic.save(dicFile)
    def reload(self):
        global dic
        dic=spruceData.XMLloader(os.path.join(modPath, "dictionary.xml"))
        for tab in self.tabs:
            tab.populate(dic)
    def __init__(self, parent=None):
        QtGui.QWidget.__init__(self, parent)
        self.ui = SpruceUI.Ui_Spruce()
        self.ui.setupUi(self)
        QtCore.QObject.connect(self.ui.bSave, QtCore.SIGNAL("clicked()"), self.save)
        QtCore.QObject.connect(self.ui.bGen, QtCore.SIGNAL("clicked()"), self.gen)
        QtCore.QObject.connect(self.ui.bReload, QtCore.SIGNAL("clicked()"), self.reload)
        QtCore.QObject.connect(self.ui.bMass, QtCore.SIGNAL("clicked()"), self.massEntry)
        self.setWindowTitle("Spruce Dictionary Editor")
        tabWidget=self.ui.tabWidget
        tabWidget.removeTab(0)
        self.NounTab=NounModel()
        tabWidget.addTab(self.NounTab, "Nouns")
        self.VerbTab=VerbModel()
        tabWidget.addTab(self.VerbTab, "Verbs")
        self.AdjTab=AdjModel()
        tabWidget.addTab(self.AdjTab, "Adjectives")
        self.AdvTab=AdvModel()
        tabWidget.addTab(self.AdvTab, "Adverbs")
        self.ConjTab=ConjModel()
        tabWidget.addTab(self.ConjTab, "Conjunctions")
        self.PrepTab=PrepModel()
        tabWidget.addTab(self.PrepTab, "Prepositions")
        self.InterjTab=InterjModel()
        tabWidget.addTab(self.InterjTab, "Interjections")
        self.tabs=(self.NounTab, self.VerbTab, self.AdjTab, self.AdvTab, self.PrepTab, self.ConjTab, self.InterjTab)
        self.reload()
    def massEntry(self):
        global dic
        self.mass=MassModel()
        self.mass.setupUi()
        self.mass.dic=dic
        self.mass.tabs=self.tabs
    def gen(self):
        Spruce.dic=dic
        QtGui.QMessageBox.information(self, "Generate Sentence", Spruce.Clause().write())

if __name__ == "__main__":
    app = QtGui.QApplication(sys.argv)
    myapp = MyForm()
    myapp.show()
    sys.exit(app.exec_())
