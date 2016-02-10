# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'MassEntry.ui'
#
# Created: Sun Jul 28 01:23:04 2013
#      by: PyQt4 UI code generator 4.10
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    def _fromUtf8(s):
        return s

try:
    _encoding = QtGui.QApplication.UnicodeUTF8
    def _translate(context, text, disambig):
        return QtGui.QApplication.translate(context, text, disambig, _encoding)
except AttributeError:
    def _translate(context, text, disambig):
        return QtGui.QApplication.translate(context, text, disambig)

class Ui_MassEntry(object):
    def setupUi(self, MassEntry):
        MassEntry.setObjectName(_fromUtf8("MassEntry"))
        MassEntry.resize(400, 400)
        MassEntry.setMinimumSize(QtCore.QSize(400, 400))
        MassEntry.setSizeGripEnabled(False)
        self.horizontalLayout = QtGui.QHBoxLayout(MassEntry)
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        self.tWords = QtGui.QPlainTextEdit(MassEntry)
        self.tWords.setObjectName(_fromUtf8("tWords"))
        self.horizontalLayout.addWidget(self.tWords)
        self.verticalLayout = QtGui.QVBoxLayout()
        self.verticalLayout.setObjectName(_fromUtf8("verticalLayout"))
        self.label = QtGui.QLabel(MassEntry)
        self.label.setObjectName(_fromUtf8("label"))
        self.verticalLayout.addWidget(self.label)
        self.rNouns = QtGui.QRadioButton(MassEntry)
        self.rNouns.setObjectName(_fromUtf8("rNouns"))
        self.verticalLayout.addWidget(self.rNouns)
        self.rVerbs = QtGui.QRadioButton(MassEntry)
        self.rVerbs.setObjectName(_fromUtf8("rVerbs"))
        self.verticalLayout.addWidget(self.rVerbs)
        self.rAdjs = QtGui.QRadioButton(MassEntry)
        self.rAdjs.setObjectName(_fromUtf8("rAdjs"))
        self.verticalLayout.addWidget(self.rAdjs)
        self.rAdvs = QtGui.QRadioButton(MassEntry)
        self.rAdvs.setObjectName(_fromUtf8("rAdvs"))
        self.verticalLayout.addWidget(self.rAdvs)
        self.rConjs = QtGui.QRadioButton(MassEntry)
        self.rConjs.setObjectName(_fromUtf8("rConjs"))
        self.verticalLayout.addWidget(self.rConjs)
        self.rPreps = QtGui.QRadioButton(MassEntry)
        self.rPreps.setObjectName(_fromUtf8("rPreps"))
        self.verticalLayout.addWidget(self.rPreps)
        self.rInterjs = QtGui.QRadioButton(MassEntry)
        self.rInterjs.setObjectName(_fromUtf8("rInterjs"))
        self.verticalLayout.addWidget(self.rInterjs)
        spacerItem = QtGui.QSpacerItem(20, 40, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Expanding)
        self.verticalLayout.addItem(spacerItem)
        self.bCancel = QtGui.QPushButton(MassEntry)
        self.bCancel.setObjectName(_fromUtf8("bCancel"))
        self.verticalLayout.addWidget(self.bCancel)
        self.bAdd = QtGui.QPushButton(MassEntry)
        self.bAdd.setObjectName(_fromUtf8("bAdd"))
        self.verticalLayout.addWidget(self.bAdd)
        self.horizontalLayout.addLayout(self.verticalLayout)

        self.retranslateUi(MassEntry)
        QtCore.QMetaObject.connectSlotsByName(MassEntry)

    def retranslateUi(self, MassEntry):
        MassEntry.setWindowTitle(_translate("MassEntry", "Mass Entry", None))
        self.label.setText(_translate("MassEntry", "Part of speech:", None))
        self.rNouns.setText(_translate("MassEntry", "Nouns", None))
        self.rVerbs.setText(_translate("MassEntry", "Verbs", None))
        self.rAdjs.setText(_translate("MassEntry", "Adjectives", None))
        self.rAdvs.setText(_translate("MassEntry", "Adverbs", None))
        self.rConjs.setText(_translate("MassEntry", "Conjunctions", None))
        self.rPreps.setText(_translate("MassEntry", "Prepositions", None))
        self.rInterjs.setText(_translate("MassEntry", "Interjections", None))
        self.bCancel.setText(_translate("MassEntry", "Cancel", None))
        self.bAdd.setText(_translate("MassEntry", "Add All", None))

