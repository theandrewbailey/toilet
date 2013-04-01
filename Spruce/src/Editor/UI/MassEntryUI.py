# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'MassEntry.ui'
#
# Created: Sat Feb  5 14:25:12 2011
#      by: PyQt4 UI code generator 4.7.4
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

class Ui_MassEntry(object):
    def setupUi(self, MassEntry):
        MassEntry.setObjectName("MassEntry")
        MassEntry.resize(400, 400)
        MassEntry.setMinimumSize(QtCore.QSize(400, 400))
        MassEntry.setSizeGripEnabled(False)
        self.horizontalLayout = QtGui.QHBoxLayout(MassEntry)
        self.horizontalLayout.setObjectName("horizontalLayout")
        self.tWords = QtGui.QPlainTextEdit(MassEntry)
        self.tWords.setObjectName("tWords")
        self.horizontalLayout.addWidget(self.tWords)
        self.verticalLayout = QtGui.QVBoxLayout()
        self.verticalLayout.setObjectName("verticalLayout")
        self.label = QtGui.QLabel(MassEntry)
        self.label.setObjectName("label")
        self.verticalLayout.addWidget(self.label)
        self.rNouns = QtGui.QRadioButton(MassEntry)
        self.rNouns.setObjectName("rNouns")
        self.verticalLayout.addWidget(self.rNouns)
        self.rVerbs = QtGui.QRadioButton(MassEntry)
        self.rVerbs.setObjectName("rVerbs")
        self.verticalLayout.addWidget(self.rVerbs)
        self.rAdjs = QtGui.QRadioButton(MassEntry)
        self.rAdjs.setObjectName("rAdjs")
        self.verticalLayout.addWidget(self.rAdjs)
        self.rAdvs = QtGui.QRadioButton(MassEntry)
        self.rAdvs.setObjectName("rAdvs")
        self.verticalLayout.addWidget(self.rAdvs)
        self.rConjs = QtGui.QRadioButton(MassEntry)
        self.rConjs.setObjectName("rConjs")
        self.verticalLayout.addWidget(self.rConjs)
        self.rPreps = QtGui.QRadioButton(MassEntry)
        self.rPreps.setObjectName("rPreps")
        self.verticalLayout.addWidget(self.rPreps)
        self.rInterjs = QtGui.QRadioButton(MassEntry)
        self.rInterjs.setObjectName("rInterjs")
        self.verticalLayout.addWidget(self.rInterjs)
        spacerItem = QtGui.QSpacerItem(20, 40, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Expanding)
        self.verticalLayout.addItem(spacerItem)
        self.bCancel = QtGui.QPushButton(MassEntry)
        self.bCancel.setObjectName("bCancel")
        self.verticalLayout.addWidget(self.bCancel)
        self.bAdd = QtGui.QPushButton(MassEntry)
        self.bAdd.setObjectName("bAdd")
        self.verticalLayout.addWidget(self.bAdd)
        self.horizontalLayout.addLayout(self.verticalLayout)

        self.retranslateUi(MassEntry)
        QtCore.QMetaObject.connectSlotsByName(MassEntry)

    def retranslateUi(self, MassEntry):
        MassEntry.setWindowTitle(QtGui.QApplication.translate("MassEntry", "Mass Entry", None, QtGui.QApplication.UnicodeUTF8))
        self.label.setText(QtGui.QApplication.translate("MassEntry", "Part of speech:", None, QtGui.QApplication.UnicodeUTF8))
        self.rNouns.setText(QtGui.QApplication.translate("MassEntry", "Nouns", None, QtGui.QApplication.UnicodeUTF8))
        self.rVerbs.setText(QtGui.QApplication.translate("MassEntry", "Verbs", None, QtGui.QApplication.UnicodeUTF8))
        self.rAdjs.setText(QtGui.QApplication.translate("MassEntry", "Adjectives", None, QtGui.QApplication.UnicodeUTF8))
        self.rAdvs.setText(QtGui.QApplication.translate("MassEntry", "Adverbs", None, QtGui.QApplication.UnicodeUTF8))
        self.rConjs.setText(QtGui.QApplication.translate("MassEntry", "Conjunctions", None, QtGui.QApplication.UnicodeUTF8))
        self.rPreps.setText(QtGui.QApplication.translate("MassEntry", "Prepositions", None, QtGui.QApplication.UnicodeUTF8))
        self.rInterjs.setText(QtGui.QApplication.translate("MassEntry", "Interjections", None, QtGui.QApplication.UnicodeUTF8))
        self.bCancel.setText(QtGui.QApplication.translate("MassEntry", "Cancel", None, QtGui.QApplication.UnicodeUTF8))
        self.bAdd.setText(QtGui.QApplication.translate("MassEntry", "Add All", None, QtGui.QApplication.UnicodeUTF8))

