package elasticsearch

import (
	"logcabin"

	"encoding/json"
	"github.com/mattbaird/elastigo/lib"

	"templeton/database"
	"templeton/model"
)

var (
	logger = logcabin.New()
)

// Elasticer is a type used to interact with Elasticsearch
type Elasticer struct {
	es      *elastigo.Conn
	baseURL string
	index   string
}

// NewElasticer returns a pointer to an Elasticer instance that has already tested its connection
// by making a WaitForStatus call to the configured Elasticsearch cluster
func NewElasticer(elasticsearchBase string, elasticsearchIndex string) (*Elasticer, error) {
	c := elastigo.NewConn()

	err := c.SetFromUrl(elasticsearchBase)
	if err != nil {
		return nil, err
	}

	_, err = c.WaitForStatus("red", 10, elasticsearchIndex)
	if err != nil {
		return nil, err
	}

	return &Elasticer{es: c, baseURL: elasticsearchBase, index: elasticsearchIndex}, nil
}

func (e *Elasticer) Close() {
	e.es.Close()
}

// PurgeIndex walks an index querying a database, deleting those which should not exist
func (e *Elasticer) PurgeIndex(d *database.Databaser) {
	indexer := e.es.NewBulkIndexerErrors(10, 60)
	indexer.Start()
	defer indexer.Stop()
}

// IndexEverything creates a bulk indexer and takes a database, and iterates to index its contents
func (e *Elasticer) IndexEverything(d *database.Databaser) {
	indexer := e.es.NewBulkIndexerErrors(10, 60)
	indexer.Start()
	defer indexer.Stop()

	cursor, err := d.GetAllObjects()
	if err != nil {
		logger.Fatal(err)
	}
	defer cursor.Close()

	for {
		ids, err := cursor.Next()
		if err == database.EOS {
			logger.Print("Done all rows, finishing.")
			break
		}
		if err != nil {
			logger.Print(err)
			break
		}

		formatted, err := model.AVUsToIndexedObject(ids)
		if err != nil {
			logger.Print(err)
			break
		}
		logger.Printf("Indexing %s", formatted.ID)

		js, err := json.Marshal(formatted)
		if err != nil {
			logger.Print(err)
			break
		}

		indexer.Index(e.index, "metadata", formatted.ID, "", "", nil, js)
	}
}
