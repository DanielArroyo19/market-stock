# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: Quotes.proto
"""Generated protocol buffer code."""
from google.protobuf.internal import builder as _builder
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from protobuf import timestamp_pb2

# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x0cQuotes.proto\x12\x06Market\x1a\x0ftimestamp.proto\"\xf1\x01\n\x05Quote\x12$\n\x06symbol\x18\x01 \x01(\x0b\x32\x14.Market.Quote.Symbol\x12(\n\x08\x63urrency\x18\x02 \x01(\x0e\x32\x16.Market.Quote.Currency\x12\x0c\n\x04last\x18\x03 \x01(\x01\x12\x0b\n\x03\x62id\x18\x04 \x01(\x01\x12\x0b\n\x03\x61sk\x18\x05 \x01(\x01\x12\x38\n\x14transactionTimestamp\x18\x06 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x1a\x18\n\x06Symbol\x12\x0e\n\x06symbol\x18\x01 \x01(\t\"\x1c\n\x08\x43urrency\x12\x07\n\x03USD\x10\x00\x12\x07\n\x03MXN\x10\x01\x42\x18\n\x16\x63om.market.stock.protob\x06proto3')

_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, globals())
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'Quotes_pb2', globals())
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\026com.market.stock.proto'
  _QUOTE._serialized_start=42
  _QUOTE._serialized_end=283
  _QUOTE_SYMBOL._serialized_start=229
  _QUOTE_SYMBOL._serialized_end=253
  _QUOTE_CURRENCY._serialized_start=255
  _QUOTE_CURRENCY._serialized_end=283
# @@protoc_insertion_point(module_scope)