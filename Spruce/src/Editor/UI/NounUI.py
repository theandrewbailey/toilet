# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Noun.ui'
#
# Created: Sun Jul 28 07:39:32 2013
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

class Ui_Form(object):
    def setupUi(self, Form):
        Form.setObjectName(_fromUtf8("Form"))
        Form.resize(598, 475)
        self.horizontalLayout = QtGui.QHBoxLayout(Form)
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        self.verticalFrame = QtGui.QFrame(Form)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Preferred)
        sizePolicy.setHorizontalStretch(200)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.verticalFrame.sizePolicy().hasHeightForWidth())
        self.verticalFrame.setSizePolicy(sizePolicy)
        self.verticalFrame.setMaximumSize(QtCore.QSize(200, 16777215))
        self.verticalFrame.setObjectName(_fromUtf8("verticalFrame"))
        self.verticalLayout = QtGui.QVBoxLayout(self.verticalFrame)
        self.verticalLayout.setMargin(0)
        self.verticalLayout.setObjectName(_fromUtf8("verticalLayout"))
        self.label = QtGui.QLabel(self.verticalFrame)
        self.label.setMaximumSize(QtCore.QSize(200, 16777215))
        self.label.setObjectName(_fromUtf8("label"))
        self.verticalLayout.addWidget(self.label)
        self.lWords = QtGui.QListWidget(self.verticalFrame)
        self.lWords.setMaximumSize(QtCore.QSize(200, 16777215))
        self.lWords.setObjectName(_fromUtf8("lWords"))
        self.verticalLayout.addWidget(self.lWords)
        self.bRemove = QtGui.QPushButton(self.verticalFrame)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Fixed, QtGui.QSizePolicy.Fixed)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.bRemove.sizePolicy().hasHeightForWidth())
        self.bRemove.setSizePolicy(sizePolicy)
        self.bRemove.setObjectName(_fromUtf8("bRemove"))
        self.verticalLayout.addWidget(self.bRemove)
        self.horizontalLayout.addWidget(self.verticalFrame)
        self.line = QtGui.QFrame(Form)
        self.line.setFrameShape(QtGui.QFrame.VLine)
        self.line.setFrameShadow(QtGui.QFrame.Sunken)
        self.line.setObjectName(_fromUtf8("line"))
        self.horizontalLayout.addWidget(self.line)
        self.gridLayout = QtGui.QGridLayout()
        self.gridLayout.setObjectName(_fromUtf8("gridLayout"))
        self.singularLabel = QtGui.QLabel(Form)
        self.singularLabel.setObjectName(_fromUtf8("singularLabel"))
        self.gridLayout.addWidget(self.singularLabel, 0, 0, 1, 1)
        self.tSingular = QtGui.QLineEdit(Form)
        self.tSingular.setObjectName(_fromUtf8("tSingular"))
        self.gridLayout.addWidget(self.tSingular, 0, 1, 1, 1)
        self.singularSyllablesLabel = QtGui.QLabel(Form)
        self.singularSyllablesLabel.setObjectName(_fromUtf8("singularSyllablesLabel"))
        self.gridLayout.addWidget(self.singularSyllablesLabel, 1, 0, 1, 1)
        self.singularSyl = QtGui.QSpinBox(Form)
        self.singularSyl.setMaximumSize(QtCore.QSize(60, 16777215))
        self.singularSyl.setObjectName(_fromUtf8("singularSyl"))
        self.gridLayout.addWidget(self.singularSyl, 1, 1, 1, 1)
        spacerItem = QtGui.QSpacerItem(20, 20, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Fixed)
        self.gridLayout.addItem(spacerItem, 2, 1, 1, 1)
        self.pluralLabel = QtGui.QLabel(Form)
        self.pluralLabel.setObjectName(_fromUtf8("pluralLabel"))
        self.gridLayout.addWidget(self.pluralLabel, 3, 0, 1, 1)
        self.tPlural = QtGui.QLineEdit(Form)
        self.tPlural.setObjectName(_fromUtf8("tPlural"))
        self.gridLayout.addWidget(self.tPlural, 3, 1, 1, 1)
        self.pluralSyllablesLabel = QtGui.QLabel(Form)
        self.pluralSyllablesLabel.setObjectName(_fromUtf8("pluralSyllablesLabel"))
        self.gridLayout.addWidget(self.pluralSyllablesLabel, 4, 0, 1, 1)
        self.pluralSyl = QtGui.QSpinBox(Form)
        self.pluralSyl.setMaximumSize(QtCore.QSize(60, 16777215))
        self.pluralSyl.setObjectName(_fromUtf8("pluralSyl"))
        self.gridLayout.addWidget(self.pluralSyl, 4, 1, 1, 1)
        spacerItem1 = QtGui.QSpacerItem(20, 20, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Fixed)
        self.gridLayout.addItem(spacerItem1, 5, 1, 1, 1)
        self.cPlural = QtGui.QCheckBox(Form)
        self.cPlural.setObjectName(_fromUtf8("cPlural"))
        self.gridLayout.addWidget(self.cPlural, 6, 1, 1, 1)
        self.cMass = QtGui.QCheckBox(Form)
        self.cMass.setObjectName(_fromUtf8("cMass"))
        self.gridLayout.addWidget(self.cMass, 7, 1, 1, 1)
        spacerItem2 = QtGui.QSpacerItem(20, 20, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Fixed)
        self.gridLayout.addItem(spacerItem2, 8, 1, 1, 1)
        self.cMasculine = QtGui.QCheckBox(Form)
        self.cMasculine.setObjectName(_fromUtf8("cMasculine"))
        self.gridLayout.addWidget(self.cMasculine, 9, 1, 1, 1)
        self.cFeminine = QtGui.QCheckBox(Form)
        self.cFeminine.setObjectName(_fromUtf8("cFeminine"))
        self.gridLayout.addWidget(self.cFeminine, 10, 1, 1, 1)
        spacerItem3 = QtGui.QSpacerItem(20, 2000, QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Expanding)
        self.gridLayout.addItem(spacerItem3, 11, 1, 1, 1)
        self.horizontalLayout_3 = QtGui.QHBoxLayout()
        self.horizontalLayout_3.setObjectName(_fromUtf8("horizontalLayout_3"))
        spacerItem4 = QtGui.QSpacerItem(40, 20, QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        self.horizontalLayout_3.addItem(spacerItem4)
        self.bRelative = QtGui.QPushButton(Form)
        self.bRelative.setObjectName(_fromUtf8("bRelative"))
        self.horizontalLayout_3.addWidget(self.bRelative)
        self.bAdd = QtGui.QPushButton(Form)
        self.bAdd.setObjectName(_fromUtf8("bAdd"))
        self.horizontalLayout_3.addWidget(self.bAdd)
        self.gridLayout.addLayout(self.horizontalLayout_3, 12, 1, 1, 1)
        self.horizontalLayout.addLayout(self.gridLayout)

        self.retranslateUi(Form)
        QtCore.QMetaObject.connectSlotsByName(Form)

    def retranslateUi(self, Form):
        Form.setWindowTitle(_translate("Form", "Nouns", None))
        self.label.setText(_translate("Form", "Nouns (Singlular):", None))
        self.bRemove.setText(_translate("Form", "Remove Entry", None))
        self.singularLabel.setText(_translate("Form", "Singular", None))
        self.singularSyllablesLabel.setText(_translate("Form", "Singular Syllables:", None))
        self.pluralLabel.setText(_translate("Form", "Plural", None))
        self.pluralSyllablesLabel.setText(_translate("Form", "Plural Syllables:", None))
        self.cPlural.setText(_translate("Form", "No Plural", None))
        self.cMass.setText(_translate("Form", "Mass Noun", None))
        self.cMasculine.setText(_translate("Form", "Masculine", None))
        self.cFeminine.setText(_translate("Form", "Feminine", None))
        self.bRelative.setText(_translate("Form", "Relatives", None))
        self.bAdd.setText(_translate("Form", "Add/Update Entry", None))

