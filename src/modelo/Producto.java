package modelo;

public class Producto {
    private String codigo;
    private String nombre;
    private String categoria;
    private String descripcion;
    private double precioUnitario;
    private boolean disponible;
    
    // Constructor
    public Producto(String codigo, String nombre, String categoria, String descripcion, double precioUnitario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.disponible = true;
    }
    
    // Getters y Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    
    @Override
    public String toString() {
        return "Código: " + codigo + " | Nombre: " + nombre + " | Categoría: " + categoria + 
               " | Precio: $" + precioUnitario + " | Disponible: " + (disponible ? "Sí" : "No");  
    }
}
