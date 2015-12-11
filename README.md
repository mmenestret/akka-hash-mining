# akka-hash-mining
A simple Hash mining program

Given a key, try to find a value given the format:

  try = concat(key,value)
  
So that MD5(try) gives a 6 leading 0 hash.
It is done by launching X actors so that each of them try to brute force an Array of Y values.
