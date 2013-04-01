from spruceNoun import *
from spruceVerb import *
import Editor.UI.MassEntryUI as MassUI
from spruceVerb import Verb
from PyQt4 import QtCore, QtGui

class MassModel(MassUI.Ui_MassEntry):
    """A window that allows entry of many, many words of one part of speech at once"""
    def setupUi(self):
        self.massContainer=QtGui.QDialog()

        # stuff, heh
        MassUI.Ui_MassEntry.setupUi(self, self.massContainer)
        QtCore.QObject.connect(self.bAdd, QtCore.SIGNAL("clicked()"), self.add)
        QtCore.QObject.connect(self.bCancel, QtCore.SIGNAL("clicked()"), self.cancel)

        self.massContainer.show()

    def add(self):
        abbrev=None
        tab=None
        if self.tWords.toPlainText()=='':
            QtGui.QMessageBox.warning(self.massContainer, "Error", "You need enter something.")
            return
        elif self.rNouns.isChecked():
            abbrev=Noun.abbrev
            tab=self.tabs[0]
        elif self.rVerbs.isChecked():
            abbrev=Verb.abbrev
            tab=self.tabs[1]
        elif self.rAdjs.isChecked():
            abbrev=Adjective.abbrev
            tab=self.tabs[2]
        elif self.rAdvs.isChecked():
            abbrev=Adverb.abbrev
            tab=self.tabs[3]
        elif self.rPreps.isChecked():
            abbrev=Preposition.abbrev
            tab=self.tabs[4]
        elif self.rConjs.isChecked():
            abbrev=Conjunction.abbrev
            tab=self.tabs[5]
        elif self.rInterjs.isChecked():
            abbrev=Interjection.abbrev
            tab=self.tabs[6]
        else:
            QtGui.QMessageBox.warning(self.massContainer, "Error", "You need to select which part of speech these are.")
            return
        entered=self.tWords.toPlainText().split('\n')
        enteredAlready=[]
        for word in entered:
            word=str(word).strip()
            if not word in tab.model and not word in enteredAlready:    # still might have dupes
                self.dic.addWord(word, abbrev)
                enteredAlready.append(word)
        self.dic.sortItAllOut()
        tab.populate(self.dic)
        self.cancel()

    def cancel(self):
        self.massContainer.reject()

