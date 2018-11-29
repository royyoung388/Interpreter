# CMM语言解释器

test.c 测试词法分析文件
production.txt LL(1)产生式规则文件
testAnalyse.c 测试预测分析表语法文件

## 词法分析 lexer包下：
Lexer 语法分析主要类
Token token类
Utils 工具类

## 语法分析 parser包下：
AnalyseProduction 从指定文件中读取LL(1)产生式规则，进行分析
AnalyseTable 通过预测分析表法，分析语法结构
Production 产生式类