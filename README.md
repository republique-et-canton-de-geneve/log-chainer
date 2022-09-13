# Open e-Gov - LogChainer

The goal of the LogChainer project is to create an unbroken sequence of log files so that any modification is registered.

The chain works as following. Consider 3 files flux1-a.log, flux1-b.log, flux1-c.log. Here the flux of all files is the same (flux1) so only their stamp changes.
Once all of these 3 files have been put by in the input directory, the system moves them to the working directory in order to process them.
The files are processed following the order given by their stamp.
For every file, a copy of the file is released into the output directory, with one difference with the original file: in the beginning of the
output file a few lines of text are inserted. These header lines establish the chaining.
In the example, the header text are the following:
- In file flux1-a.log :

```
<Previous file: none>

<Date of chaining: ...date...>
 
<SHA-256: >
```

- In file flux1-b.log :

```
<Previous file: flux1-a.log>

<Date of chaining: ...date...>
 
<SHA-256: ...hashCode of previous file...>
```

- In file flux1-c.log :

```
<Previous file: flux1-b.log>

<Date of chaining: ...date...>
 
<SHA-256: ...hashCode of previous file...>
```
 
Additionally, the system retains only file flux1-c.log in the working directory.

Note. This project only supports the log chaining of provided files: if a version of the file is not submitted, no error
will be sent - an undetected breach will silently occur. It is therefore up to the user to set correctly the history
of the modifications. If a file does not have an appropriate name, it will be moved to the corrupted files directory
and the system won't further process it.


# Table of contents
- [Overview](#overview)
    - [Contents](#contents)
    - [Use](#use)
- [Installation](#installation)
- [Configuration](#configuration)
	- [Execution](#execution)
	- [Launcher](#launcher)
	- [Properties](#properties)
		- [Application](#application)
		- [Error](#error)
		- [Logback](#logback)
	- [Components](#components)
- [Functioning](#functioning)
	- [Process](#process)
	- [Exceptions](#exceptions)
	- [Monitoring](#monitoring)
- [Contact](#contact)

# Overview 

## Contents

The project also contains a working copy of the log chainer. The ZIP version contains a functional launcher, the JAR file
of the log chainer, the properties file, and an XML configuration file.

## Use cases

Here are two example case of use for the logchainer application: 

- the application can be launched once and left functioning in the background.
Then the output of the application producing the files to chain can be plugged on the system's input directory so that the files are continuously chained.
- once in a while the application can be launched and the user can put all of his files to chain at once and then shut down the application.

# Installation

1. Download the ZIP file
2. Unzip the file locally 
3. On Linux, set execution rights to the launcher: ``chmod +x ./bin/run.sh``
4. External configurations examples are already shipped with the ZIP file, so nothing has to be created in order to launch the project. As regards custom external configurations, see section Configuration
5. From the folder bin, launch the project: ``./run.sh``

# Configuration

All external configuration files are given in the downloaded ZIP file. These files can be used as examples for the variable names
(especially in the properties file where it is vital to use the same name) and for the file and directory structure.

## Execution

You can run the project from anywhere. However, you will have to create all necessary directories before launching.
Also, you must use a 1.8 version of the JDK ; if you want to use a more recent version of the JDK, such as
JDK 11 or JDK 17, you will have at the very least to update the version of the `jaxb2-maven-plugin`.

## Launcher

The launcher ``run.sh`` must contain: 
- the path to the JVM. Alternatively the JAVA_HOME system property can be set
- the path to the project JAR file
- the paths to the 3 properties files:
	- the project properties (-Dapplication.properties=&lt;PATH&gt;)
	- the error messages properties (-DerrorMessages.properties=&lt;PATH&gt;)
	- the Logback properties (-Dlogback.properties=&lt;PATH&gt;)

## Properties

### Application

The file is designated by "application.properties". It must contain the path to the XML file (where the clients are defined).

### Error

The file is designated by "errorMessages.properties". It must contain all of the error messages related to the handled errors which can be raised by the system. 
Note that the use of braces ({}) can seem strange at first. It stems from the fact that these messages are used "as-is" in the logger from the Logback library,
therefore the messages should comply with the Logback syntax.

### Logback

The file is designated by "logback.properties". It provides the logging level of the project.

## Components

The LogChainer system can handle several clients simultaneously. A client is a business configuration of the following properties:
- ClientId: name of the client.
- InputDir: path of the directory where the system gets the files to be processed.
The directory must be created upfront by the user.
The files will only stay in this directory until they are processed. The directory ends up empty.
- OutputDir: path of the directory where the system moves the files after processing them.
The directory must be created upfront by the user.
- WorkingDir: path of the directory internally used by the system to store the files being processed. 
The directory must be created upfront by the user.
Once a file has been processed, it is moved to the output directory. However a copy is kept in the working directory in order
to establish a chain. The chain is only created from the files already contained in this directory. 
That is, if one file is missing, the system won't notice the absence and will continue as if nothing wrong happened, 
because the system is designed for chaining the previous file from the flux by taking the file present in this directory.
No file should be deleted from this directory in any other circumstances than the end of the flux chain.
- CorruptedFilesDir: path of the directory where the files with an invalid name will be moved (without any processing).
The directory must be created upfront by the user.
- Separator: character for the separation between the flux name and the sorting stamp. Default is "_".
- SortingType: type of sorting requested. Expected values: 
	- "alphabetical" : alphabetical order.
	- "numerical" : numerical order (default).
- StampPosition: position of the sorting stamp in the filename, relative to the flux. Expected values:
	- "before": when the sorting stamp stands before the flux name in the file name.
	- "after": when the sorting stamp stands after the flux name in the file name (default).
- FileEncoding: encoding of the files in a client. The string given should comply with a Java Charset to be valid (default is "UTF-8").

The clients are defined in the XML file.

# Functioning

Once the program is launched, for every client in the XML file, the system creates a WatchService (directory surveillance)
and attaches it to the client. Each client refers to an input directory where files will be put by the user and then detected. 
The WatchService will be plugged on this directory.
The working directory where each file will be moved once detected to be processed contains a copy of each flux' last file.
The file will be temporarily kept in the working directory to establish a history of the flux' files and thus to chain them. 
The output directory contains the processed files. It is up to the user to get the files from that directory. 
In the case of an invalid filename, the file won't be processed; it will instead be moved to the corrupted files directory.
It is up to the user to get the files from that directory.
Notes:
- If the user suppresses a file from the working directory, the chain will be broken but no error will be thrown.
- No file with the same name can be present in the working directory when a file arrives in it (the stamp should prevent those kind of error).
- When a new file arrives in the output directory, if a file with the same name already exists, it will be overwritten by the new one.

## Process

- Before file process
   1. The clients are instanciated together with their attributes - a watcher and a watchKey.
   They are placed in a list that will be run through endlessly.
   2. For each iteration on a client, we detect the arrival of a file(maximum on detection by iteration so in case of grouped arrival, the detection will take as many iteration on the client as there are files).
   3. The file name is checked. If it is invalid, the file is put in the corrupted directory without being processed.
   4. The characteristics of the flux (name, stamp, arrival date) are set up. The flux name is mapped to the list of all its files.
   5. For each iteration on a client, the system iterates on the list of its fluxes. For each iteration on a flux, the system iterates 
   on all its files.
   6. A file is said to be ready to be processed after an arbitrary lapse of time (7 seconds) after its detection. The flux is processed 
   once all files are ready.
   7. The system starts the flux process and iterates over all files according to the sorting order.
    The sorting order is defined in the XML file by the stamp and the sorting type.

- File process
   1. The system searches the working directory for a file of the same flux. If such a file is present, the system gets the file's 
   hash code and removes it (it will be the previous file in the chain).
   2. The system moves the file being processed to the working directory.
   3. The system inserts the previous file's hash code at the beginning of the current file, if such previous file exists.
    If it doesn't exist, the system inserts an empty hash (arbitrary convention), as well as the previous file's name and the chaining date.

- After file process
   1. The current file is copied into the output directory. A copy is kept in the working directory for history.
   2. If the file was the last file of its flux, the flux is removed from the client's flux list.
   3. Execution proceeds to the next iteration on the flux and on the clients.

## Exceptions

This section provides the list of the expected exceptions. Any other exception that might be raised won't be handled. Legend: (I) stands for: the exception generates a program interruption. (NI) stands for: the exception generates no program interruption.

- NameException: (ClientService::registerEvent) the pattern of the name is incorrect. (NI)
- FileAlreadyExistsException: (FolderService::moveFileInDirWithNoSameNameFile) a file with the same name already exists in the directory. (I)
- FileNotFoundException/NoSuchFileException: 
	- (FolderService::moveFileInDirWithNoSameNameFile) the file couldn't be found in the input directory (possibly it was detected but it was removed before being processed). (I)
	- (LogChainerService::chainingLogFile) (2 occurrences) the files were not found in the working directory. (I)
	- (LogWatcherService::getPreviousFileHash) at stream opening time, the previous file wasn't found in the working directory. (I)
- IOException: 
	- (FolderService::moveFileInDirWithNoSameNameFile) when an IOException is raised in Files::move. (I)
	- (FolderService::copyFileToDirByReplacingExisting) when an IOException is raised in Files::copy. (I)
	- (HashService::getLogHash) error when reading the given stream. (I)
	- (LogChainerService::chainingLogFile) error when reading the stream opened on the file to process. (I)
	- (LogChainerService::accessToTmpFile) error when reading the stream opened on the temp file. (I)
	- (LogChainerService::insertionOfMessage) error while manipulating the streams. (I)
	- (LogWatcherService::initializeFileWatcherByClient) error while initializing the watchKey. (I)
	- (LogWatcherService::getPreviousFileHash) error when reading the stream from the previous file in the working directory. (I)
- UnsupportedEncodingException: (LogWatcherService::newFileTreatment) encoding type isn't a valid Charset. (I)
- JAXBException: (LogChainer::loadConfiguration) error while loading the configurations from the XML file. (I)
- CorruptedKeyException: (LogWatcherService::processEvents) the directory isn't accessible anymore or the key is corrupted. (I)
- WatchServiceError: (Client::constructor) error while creating the watchService. (I)

## Monitoring

Monitoring of the application can be accessed at URL ``localhost:8080`` .
You can monitor the following elements in addition to the default ones:
- The status of the system's health. It will only tell you if the application is still running (UP) or not (DOWN). In order to access 
this information, you must  add ``/actuator/health`` to the monitoring address.
- Information on all detected corrupted files. This provides the total number of corrupted files that have been transferred 
by the program and a summary of all clients related to the number of corrupted files with their name and their size.
Add ``/actuator/global/`` to the monitoring address.
- Information on the detected corrupted files for a particular client. This provides the number of corrupted files for the 
specified client and a summary of these files with their name and size. Add ``/actuator/{clientName}`` to the monitoring address.

# Contact

Should you have any question, comment or proposal, please send an email to opensource@etat.ge.ch.
