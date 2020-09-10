package com.exo.scomm.utils.mapper;

public interface IMapper<From, To> {
   To map(From from);
}