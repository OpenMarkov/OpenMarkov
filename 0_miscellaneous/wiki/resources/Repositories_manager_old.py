import os
import subprocess

debug = False

def debugging():
    try:
        os.chdir("org.openmarkov")
        output = subprocess.check_output("git pull",stderr=subprocess.STDOUT,shell=True).decode("utf-8")
        for line in output.split("\n"):
            print (line)
        os.chdir("..")
    except subprocess.CalledProcessError as e:
        # 255 = not found - module or branch.
        if (e.returncode == 255):
            print("Error: No repository found")
        print ("ERROR: ", e.returncode)

def checkout(branchName):
    file = open('repositories', 'r')
    i = 0
    for line in file:
        module = line.strip()
        print ("Processing module: " + module)
        exists = os.path.exists(module)
        if exists:
            os.chdir(module)
            os.system("git checkout " + branchName)
            os.chdir("..")
            i = i + 1
        else:
            print ("The module " + module + " doesn't exists")
        if 'str' in line:
            break
    print ('Checkout modules: ' + str(i))


def cloneAllRepos(branchName):
    file = open('repositories', 'r')
    bitbucketPath = "https://bitbucket.org/cisiad/"
    i = 0
    for line in file:
        module = line.strip()
        print ("Processing module: " + module)
        exists = os.path.exists(module)
        if exists:
            print ("The module " + module + " already exists")
        else:
            os.system("git clone " + bitbucketPath + module + " -b " + branchName)
            i = i + 1
        if 'str' in line:
            break
    print ('Cloned modules: ' + str(i))

def pullAllRepos():
    print ("Not implemented yet")
    return
    file = open('repositories', 'r')
    i = 0
    for line in file:
        module = line.strip()
        print ("Processing module: " + module)
        exists = os.path.exists(module)
        if exists:
            os.system("git pull -R " + module)
            i = i + 1
        else:
            print ("The module " + module + " doesn't exists")
        if 'str' in line:
            break
    print ('Pulled modules: ' + str(i))

def pushAllRepos():
    file = open('repositories', 'r')
    i = 0
    for line in file:
        module = line.strip()
        print ("Processing module: " + module)
        exists = os.path.exists(module)
        if exists:
            os.chdir(module)
            os.system("git push")
            os.chdir("..")
            i = i + 1
        else:
            print ("The module " + module + " doesn't exists")
        if 'str' in line:
            break
    print ('Checkout modules: ' + str(i))
    
def showMenu():
    option = "0"
    while (option != "5"):
        _ = os.system('cls' if os.name == 'nt' else 'clear')
        print("OpenMarkov code script")
        print("======================")
        if debug:
            print("0. Debug function.")
        print("1. Pull all repositories.")
        print("2. Checkout all repositories.")
        print("3. Clone all repositories.")
        print("4. Push all repositories.")
        print("5. Exit.")
        option = input()
        if (debug and option == "0"):
            debugging()
        elif (option == "1"):
            pullAllRepos()
        elif (option == "2"):
            showUpdateMenu()
        elif (option == "3"):
            showCloneMenu()
        elif (option == "4"):
            pushAllRepos()
        elif (option == "5"):
            print("Goodbye!!")
            exit()
        else:
            print("Please enter a valid number.")
        input("Press Enter to continue.")

def showCloneMenu():
    option = "0"
    while (option != "3"):
        _ = os.system('cls' if os.name == 'nt' else 'clear')
        print("Clone menu")
        print("======================")
        print("1. Clone to master branch.")
        print("2. Clone to development branch.")
        print("3. Back.")
        option = input()
        if (option == "1"):
            cloneAllRepos("master")
        elif (option == "2"):
            cloneAllRepos("development")
        elif (option == "3"):
            showMenu()
        else:
            print("Please enter a valid number.")
        input("Press Enter to continue.")

def showUpdateMenu():
    option = "0"
    while (option != "3"):
        _ = os.system('cls' if os.name == 'nt' else 'clear')
        print("Checkout menu")
        print("======================")
        print("1. Checkout to master branch.")
        print("2. Checkout to development branch.")
        print("3. Back.")
        option = input()
        if (option == "1"):
            checkout("master")
        elif (option == "2"):
            checkout("development")
        elif (option == "3"):
            showMenu()
        else:
            print("Please enter a valid number.")
        input("Press Enter to continue.")

showMenu()
