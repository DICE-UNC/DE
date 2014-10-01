package main

import (
	"os"
	"sort"
	"strings"
	"testing"
)

func TestInodeFromPath(t *testing.T) {
	path := "test_config.json"
	inode, err := InodeFromPath(path)
	if err != nil {
		t.Error(err)
	}
	if inode <= 0 {
		t.Errorf("inode set to %d", inode)
	}
}

func TestInodeFromFile(t *testing.T) {
	openFile, err := os.Open("test_config.json")
	if err != nil {
		t.Error(err)
	}
	inode, err := InodeFromFile(openFile)
	if err != nil {
		t.Error(err)
	}
	if inode <= 0 {
		t.Errorf("inode set to %d", inode)
	}
}

func TestNewTombstoneFromPath(t *testing.T) {
	path := "test_events.txt"
	tombstone, err := NewTombstoneFromPath(path)
	if err != nil {
		t.Error(err)
	}
	if tombstone == nil {
		t.Error("tombstone is nil")
	}

	if tombstone.CurrentPos < 0 {
		t.Errorf("CurrentPos is set to %d", tombstone.CurrentPos)
	}

	if tombstone.Date.IsZero() {
		t.Error("Date has not been set")
	}

	if tombstone.LogLastMod.IsZero() {
		t.Error("LogLastMod has not been set")
	}

	if tombstone.Inode == 0 {
		t.Error("Inode was set to zero")
	}
}

func TestNewTombstoneFromFile(t *testing.T) {
	openFile, err := os.Open("test_events.txt")
	if err != nil {
		t.Error(err)
	}
	tombstone, err := NewTombstoneFromFile(openFile)
	if err != nil {
		t.Error(err)
	}
	if tombstone == nil {
		t.Error("tombstone is nil")
	}

	if tombstone.CurrentPos < 0 {
		t.Errorf("CurrentPos is set to %d", tombstone.CurrentPos)
	}

	if tombstone.Date.IsZero() {
		t.Error("Date has not been set")
	}

	if tombstone.LogLastMod.IsZero() {
		t.Error("LogLastMod has not been set")
	}

	if tombstone.Inode == 0 {
		t.Error("Inode was set to zero")
	}
}

func TestWriteToFile(t *testing.T) {
	path := "test_events.txt"
	tombstone, err := NewTombstoneFromPath(path)
	if err != nil {
		t.Error(err)
	}
	if tombstone == nil {
		t.Error("tombstone is nil")
	}
	err = tombstone.WriteToFile()
	if err != nil {
		t.Error(err)
	}
	of, err := os.Open(TombstonePath)
	if err != nil {
		t.Error(err)
	}
	err = of.Close()
	if err != nil {
		t.Error(err)
	}
	err = os.Remove(TombstonePath)
	if err != nil {
		t.Error(err)
	}
}

func TestReadTombstone(t *testing.T) {
	path := "test_events.txt"
	t1, err := NewTombstoneFromPath(path)
	if err != nil {
		t.Error(err)
	}
	if t1 == nil {
		t.Error("tombstone is nil")
	}
	err = t1.WriteToFile()
	if err != nil {
		t.Error(err)
	}
	tombstone, err := ReadTombstone()
	if err != nil {
		t.Error(err)
	}
	if tombstone == nil {
		t.Error("tombstone is nil")
	}

	if tombstone.CurrentPos < 0 {
		t.Errorf("CurrentPos is set to %d", tombstone.CurrentPos)
	}

	if tombstone.Date.IsZero() {
		t.Error("Date has not been set")
	}

	if tombstone.LogLastMod.IsZero() {
		t.Error("LogLastMod has not been set")
	}

	if tombstone.Inode == 0 {
		t.Error("Inode was set to zero")
	}
	err = os.Remove(TombstonePath)
	if err != nil {
		t.Error(err)
	}
}

func TestNewLogfileList(t *testing.T) {
	dirname := "test_dir"
	filename := "event_log"
	ll, err := NewLogfileList(dirname, filename)
	if err != nil {
		t.Error(err)
	}
	if len(ll) <= 0 {
		t.Errorf("length of logfilelist is %d", len(ll))
	}
	if len(ll) != 5 {
		t.Errorf("length of logfilelist was %d, not 5", len(ll))
	}
	for idx, lf := range ll {
		if lf.BaseDir != "test_dir" {
			t.Errorf("BaseDir for index %d was not set to %s", idx, dirname)
		}
		info := lf.Info
		if !strings.HasPrefix(info.Name(), filename) {
			t.Errorf("Filename %s does not start with %s", info.Name(), filename)
		}
	}
}

func TestSortLogfileList(t *testing.T) {
	dirname := "test_dir"
	filename := "event_log"
	ll, err := NewLogfileList(dirname, filename)
	if err != nil {
		t.Error(err)
	}
	if len(ll) <= 0 {
		t.Errorf("length of logfilelist is %d", len(ll))
	}
	if len(ll) != 5 {
		t.Errorf("length of logfilelist was %d, not 5", len(ll))
	}
	sort.Sort(ll)
	if ll[0].Info.Name() != "event_log.4" {
		t.Errorf("Entry %d was not %s", 0, "event_log.4")
	}

}
