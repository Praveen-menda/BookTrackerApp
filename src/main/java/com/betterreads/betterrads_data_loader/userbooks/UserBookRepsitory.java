package com.betterreads.betterrads_data_loader.userbooks;

import org.springframework.data.cassandra.repository.CassandraRepository;

public interface UserBookRepsitory extends CassandraRepository<UserBooks,UserBookPrimaryKey> {
    
}
