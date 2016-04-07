# EasyAPI

TODO: Add local memory cache factory, use local available memory.

## Server cluster
Server cluster consists of a plurality of nodes. All of nodes are scheduled by the Zookeeper.

### Cache System
The data read from database can save to cache system, to increase the loading speed.

#### Redis
Cache data to Redis.

#### Tachyon
Cache data to Tachyon clusters.

### Database
Support separate read and write


## Client
The client embedded in the application used for communicating with the server cluster.

## UI
The UI is used for monitoring the status of cluster.
