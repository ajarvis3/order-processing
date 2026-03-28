-- Insert sample inventory row (runs after table creation files due to lexicographic order)

INSERT INTO inventory_db.inventory (
  id,
  sku,
  quantity_available
) VALUES (
  3,
  'SKU-001',
  100
)
ON DUPLICATE KEY UPDATE
  sku = VALUES(sku),
  quantity_available = VALUES(quantity_available);

