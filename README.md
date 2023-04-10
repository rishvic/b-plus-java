# B+ Tree Demo

A demonstration of B+ Tree, written in Java.

## Building

To compile the project, Java 8+ is required. To build the application, run

```shell
./mvnw clean package appassembler:assemble
```

The built directory will be in `target/appassembler/`. In the `bin/` folder
there, you will find `b+demo` shell script and `b+demo.bat` batch file.

## Executing

To execute the project, run the appropriate shell/batch file. To see all
available options, run `b+demo -h`.

This will start an interactive shell, where the following commands can be run:

- `add [INT [INT ...]]`: Add all specified integers into the B+ tree.
- `remove [INT [INT ...]]`: Remove all specified integers into the B+ tree.
- `contains [INT [INT ...]]`: Checks if tree contains values.
- `print`: Print the current B+ tree
- `clear`: Clears the B+ tree.

Example:

```
$ ./b+demo -B 4
>>> add 25 16 9 1 4
>>> print
[16]
├─[1, 4, 9]
└─[16, 25]
>>> add 20 13 15 10 11 12
>>> print
[13]
├─[9, 11]
│  ├─[1, 4]
│  ├─[9, 10]
│  └─[11, 12]
└─[16]
   ├─[13, 15]
   └─[16, 20, 25]
>>> remove 13 15
>>> print
[11]
├─[9]
│  ├─[1, 4]
│  └─[9, 10]
└─[13]
   ├─[11, 12]
   └─[16, 20, 25]
>>> contains 5 10 15
5: false
10: true
15: false
>>> remove 1
>>> print
[11, 13]
├─[4, 9, 10]
├─[11, 12]
└─[16, 20, 25]
>>> exit
```
