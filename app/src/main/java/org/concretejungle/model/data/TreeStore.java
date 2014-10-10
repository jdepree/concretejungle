package org.concretejungle.model.data;

import android.util.SparseArray;

import org.concretejungle.model.Tree;
import org.concretejungle.model.TreeType;
import org.concretejungle.model.User;

import java.util.Hashtable;
import java.util.List;

public class TreeStore {
    private static TreeStore mInstance;

    private Long mTimestamp;
    private List<Tree> mTreeList;
    private List<TreeType> mTreeTypeList;
    private SparseArray<Tree> mTrees;
    private SparseArray<TreeType> mTreeTypes;

    public static TreeStore getInstance() {
        if (mInstance == null) {
            mInstance = new TreeStore();
        }
        return mInstance;
    }

    public Long getLastUpdatedTimestamp() {
        return mTimestamp;
    }

    public Tree getTree(int treeId) {
        if (mTrees == null) {
            return null;
        }
        return mTrees.get(treeId);
    }

    public List<Tree> getTreeList() {
        return mTreeList;
    }

    public void setTrees(List<Tree> trees) {
        if (mTrees == null) {
            mTrees = new SparseArray<Tree>();
        }
        for (Tree tree : trees) {
            mTrees.put(tree.getId(), tree);
        }
        mTreeList = trees;
    }

    public TreeType getTreeType(int typeId) {
        return mTreeTypes.get(typeId);
    }

    public List<TreeType> getTreeTypes() {
        return mTreeTypeList;
    }

    public void setTreeTypes(List<TreeType> types) {
        if (mTreeTypes == null) {
            mTreeTypes = new SparseArray<TreeType>();
        }
        for (TreeType type : types) {
            mTreeTypes.put(type.getId(), type);
        }
        mTreeTypeList = types;
    }

}
