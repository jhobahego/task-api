package com.jhobadev.taskapi.mapper;

public interface IMapper <I, o>{
    o map(I in);
}
