package parser;

import lexer.Token;

/**
 * 语法树结点
 */
public class TreeNode {
    String cate = null;
    TreeNode[] next = null;
    TreeNode pre = null;
    //叶子结点会有一个token属性
    Token token = null;

    public TreeNode(TreeNode pre, String cate) {
        this.pre = pre;
        this.cate = cate;
    }

    public TreeNode(TreeNode pre, Token token) {
        this.token = token;
    }

    /**
     * 寻找当前结点下，与参数结点相邻的下一个结点。如果没有相邻，则返回自身的相邻
     *
     * @param treeNode
     * @return
     */
    public TreeNode findNext(TreeNode treeNode) {
        int index = 0;

        for (int i = 0; i < this.next.length; i++) {
            if (this.next[i] == treeNode) {
                index = i + 1;
                break;
            }
        }

        if (index >= this.next.length) {
            //如果 this.pre 是根节点
            if (this.pre.pre == null) {
                return this.pre;
            } else {
                return this.pre.findNext(this);
            }
        }
        else
            return this.next[index];
    }
}
