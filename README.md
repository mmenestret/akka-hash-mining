# akka-hash-mining
A simple Hash mining program

Given a key, try to find a value given the format::

  concat(keyvalue)
  
So that MD5(concat(keyvalue)) gives a 6 leading 0 hash
