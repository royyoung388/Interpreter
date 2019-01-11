# CMM语言解释器

test.c 测试词法分析文件  
production.txt LL(1)产生式规则文件  
testAnalyse.c 测试语法，语义，javacc

## 词法分析 lexer包下：
Lexer 语法分析主要类  
Token token类  
Utils 工具类  

## 语法分析 parser包下：
AnalyseProduction 从指定文件中读取LL(1)产生式规则，进行分析  
AnalyseTable 通过预测分析表法，分析语法结构  
Production 产生式类

## 语义分析 semantic包下：
Semantic 主要的分析类，使用自递归下降分析法  
Expression 中缀转后缀，计算值  
Utils 计算各种类型转换问题

## JavaCC javacc包下：
CMM 主要的javacc，使用语义子程序实现语义分析，主要逻辑类似语义分析  