import os
import subprocess
from tkinter import *
import urllib.request
import codecs
import logging

debug = False
button_width = 45
buttons = []

logging.getLogger().setLevel(logging.INFO)
bitbucketPath = "https://bitbucket.org/cisiad/"
repositoriesPath = bitbucketPath + 'org.openmarkov/wiki/resources/repositories.txt'

local_filename, headers = urllib.request.urlretrieve(bitbucketPath + 'org.openmarkov/wiki/resources/repositories.txt')
data = urllib.request.urlopen(repositoriesPath)

def checkoutAllRepos(branchName):
    file = codecs.open(local_filename, 'r', 'utf-8')
    i = 0
    for line in file:
        module = line.strip()
        logging.info("Processing module: " + module)
        exists = os.path.exists(module)
        if exists:
            os.system("git -C " + module + " checkout --progress " + branchName)
            i = i + 1
        else:
            logging.error("The module " + module + " doesn't exists")
        if 'str' in line:
            break
    logging.info('Checkout modules: ' + str(i))


def cloneAllRepos(branchName):
    file = codecs.open(local_filename, 'r', 'utf-8')
    i = 0
    for line in file:
        module = line.strip()
        logging.info("Processing module: " + module)
        exists = os.path.exists(module)
        if exists:
            logging.warning("The module " + module + " already exists")
        else:
            os.system("git clone " + bitbucketPath + module + " -b " + branchName)
            i = i + 1
        if 'str' in line:
            break
    logging.info('Cloned modules: ' + str(i))


def pullReposCallBack():
    file = codecs.open(local_filename, 'r', 'utf8')
    i = 0
    for line in file:
        module = line.strip()
        logging.info("Processing module: " + module)
        exists = os.path.exists(module)
        if exists:
            os.system("git -C " + module + " fetch origin --progress --prune")
            i = i + 1
        else:
            logging.error("The module " + module + " doesn't exists")
        if 'str' in line:
            break
    logging.info('Pulled modules: ' + str(i))

def changeBranchMasterCallBack():
    checkoutAllRepos("master")

def changeBranchDevelopmentCallBack():
    checkoutAllRepos("development")

def changeBranchReposCallBack():
    for button in buttons:
        button.pack_forget()
    changeToMaster_button.pack(side=TOP)
    changeToDevelopment_button.pack(side=TOP)
    backToMainMenu_button.pack(side=TOP)

def  cloneReposMasterCallBack():
    cloneAllRepos("master")

def cloneReposDevelopmentCallBack():
    cloneAllRepos("development")

def cloneReposCallBack():
    for button in buttons:
        button.pack_forget()

    cloneMaster_button.pack(side=TOP)
    cloneDevelopment_button.pack(side=TOP)
    backToMainMenu_button.pack(side=TOP)

def mainMenu():
    for button in buttons:
        button.pack_forget()

    cloneRepos_button.pack(side=TOP)
    pullRepos_button.pack(side=TOP)
    changeBranch_button.pack(side=TOP)


# showMenu()
top = Tk()
top.title("Repositories manager")
top.geometry("300x200")

# Buttons
buttons = []
#Main menu buttons
cloneRepos_button = Button(top, text="Clone repositories", command=cloneReposCallBack, width=button_width)
buttons.append(cloneRepos_button)
pullRepos_button = Button(top, text="Pull repositories", command=pullReposCallBack, width=button_width)
buttons.append(pullRepos_button)
changeBranch_button = Button(top, text="Change branch of all repositories", command=changeBranchReposCallBack, width=button_width)
buttons.append(changeBranch_button)

#Clone menu buttons
cloneMaster_button = Button(top, text="Clone to master branch", command=cloneReposMasterCallBack, width=button_width)
buttons.append(cloneMaster_button)
cloneDevelopment_button = Button(top, text="Clone to development branch", command=cloneReposDevelopmentCallBack, width=button_width)
buttons.append(cloneDevelopment_button)
backToMainMenu_button = Button(top, text="Back to main menu", command=mainMenu, width=button_width)
buttons.append(backToMainMenu_button)

#Change branch menu
changeToMaster_button = Button(top, text="Change to master branch", command=changeBranchMasterCallBack, width=button_width)
buttons.append(changeToMaster_button)
changeToDevelopment_button = Button(top, text="Change to development branch", command=changeBranchDevelopmentCallBack, width=button_width)
buttons.append(changeToDevelopment_button)


mainMenu()
top.mainloop()


