package com.acme.ecommerce.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.acme.ecommerce.Application;
import com.acme.ecommerce.domain.Product;
import com.acme.ecommerce.domain.ProductPurchase;
import com.acme.ecommerce.domain.Purchase;
import com.acme.ecommerce.domain.ShoppingCart;
import com.acme.ecommerce.exception.ProductOrderQuantityExceedsAvailabilityException;
import com.acme.ecommerce.service.ProductService;
import com.acme.ecommerce.service.PurchaseService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class CartControllerTest {

  final String BASE_URL = "http://localhost:8080/";
  @Mock
  private MockHttpSession session;
  @Mock
  private ProductService productService;
  @Mock
  private PurchaseService purchaseService;
  @Mock
  private ShoppingCart sCart;
  @InjectMocks
  private CartController cartController;
  private MockMvc mockMvc;

  @Before
  public void setup() {
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/");
    viewResolver.setSuffix(".html");

    MockitoAnnotations.initMocks(this);
    mockMvc =
        MockMvcBuilders.standaloneSetup(cartController).setViewResolvers(viewResolver).build();
  }

  @Test
  public void viewCartTest() throws Exception {
    Product product = productBuilder();

    when(productService.findById(1L)).thenReturn(product);

    Purchase purchase = purchaseBuilder(product);

    when(sCart.getPurchase()).thenReturn(purchase);
    mockMvc.perform(MockMvcRequestBuilders.get("/cart")).andDo(print())
        .andExpect(status().isOk())
        .andExpect(view().name("cart"));
  }

  @Test
  public void viewCartNoPurchasesTest() throws Exception {

    when(sCart.getPurchase()).thenReturn(null);
    mockMvc.perform(MockMvcRequestBuilders.get("/cart")).andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/error"));
  }

  @Test
  public void addToCartTest() throws Exception {
    Product product = productBuilder();

    when(productService.findById(1L)).thenReturn(product);

    mockMvc.perform(
        MockMvcRequestBuilders.post("/cart/add").param("quantity", "1").param("productId", "1"))
        .andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/product/"))
        .andExpect(flash().attributeExists("flash"));
  }

  @Test
  public void addUnknownToCartTest() throws Exception {
    when(productService.findById(1L)).thenReturn(null);

    mockMvc.perform(
        MockMvcRequestBuilders.post("/cart/add").param("quantity", "1").param("productId", "1"))
        .andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/error"));
  }

  @Test
  public void updateCartTest() throws Exception {
    Product product = productBuilder();

    when(productService.findById(1L)).thenReturn(product);

    Purchase purchase = purchaseBuilder(product);

    when(sCart.getPurchase()).thenReturn(purchase);

    mockMvc.perform(MockMvcRequestBuilders.post("/cart/update").param("newQuantity", "2")
        .param("productId", "1"))
        .andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"))
        .andExpect(flash().attributeExists("flash"));
  }

  @Test
  public void updateUnknownCartTest() throws Exception {
    when(productService.findById(1L)).thenReturn(null);

    mockMvc.perform(MockMvcRequestBuilders.post("/cart/update").param("newQuantity", "2")
        .param("productId", "1"))
        .andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/error"));
  }

  @Test
  public void updateInvalidCartTest() throws Exception {

    when(sCart.getPurchase()).thenReturn(null);

    mockMvc.perform(MockMvcRequestBuilders.post("/cart/update").param("newQuantity", "2")
        .param("productId", "1"))
        .andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/error"));
  }

  @Test
  public void removeFromCartTest() throws Exception {
    Product product = productBuilder();

    Product product2 = productBuilder();
    product2.setId(2L);

    when(productService.findById(1L)).thenReturn(product);

    ProductPurchase pp = new ProductPurchase();
    pp.setProductPurchaseId(1L);
    pp.setQuantity(1);
    pp.setProduct(product);

    ProductPurchase pp2 = new ProductPurchase();
    pp2.setProductPurchaseId(2L);
    pp2.setQuantity(2);
    pp2.setProduct(product2);

    List<ProductPurchase> ppList = new ArrayList<ProductPurchase>();
    ppList.add(pp);
    ppList.add(pp2);

    Purchase purchase = new Purchase();
    purchase.setId(1L);
    purchase.setProductPurchases(ppList);

    when(sCart.getPurchase()).thenReturn(purchase);

    when(purchaseService.save(purchase)).thenReturn(purchase);

    mockMvc.perform(MockMvcRequestBuilders.post("/cart/remove").param("productId", "1"))
        .andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"))
        .andExpect(flash().attributeExists("flash"));
  }

  @Test
  public void removeUnknownCartTest() throws Exception {
    when(productService.findById(1L)).thenReturn(null);

    mockMvc.perform(MockMvcRequestBuilders.post("/cart/remove").param("productId", "1"))
        .andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/error"));
  }

  @Test
  public void removeInvalidCartTest() throws Exception {

    when(sCart.getPurchase()).thenReturn(null);

    mockMvc.perform(MockMvcRequestBuilders.post("/cart/remove").param("productId", "1"))
        .andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/error"));
  }

  @Test
  public void removeLastFromCartTest() throws Exception {
    Product product = productBuilder();

    when(productService.findById(1L)).thenReturn(product);

    Purchase purchase = purchaseBuilder(product);

    when(sCart.getPurchase()).thenReturn(purchase);

    when(purchaseService.save(purchase)).thenReturn(purchase);

    mockMvc.perform(MockMvcRequestBuilders.post("/cart/remove").param("productId", "1"))
        .andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/product/"));
  }

  @Test
  public void emptyCartTest() throws Exception {
    Product product = productBuilder();

    Product product2 = productBuilder();
    product2.setId(2L);

    when(productService.findById(1L)).thenReturn(product);

    ProductPurchase pp = new ProductPurchase();
    pp.setProductPurchaseId(1L);
    pp.setQuantity(1);
    pp.setProduct(product);

    ProductPurchase pp2 = new ProductPurchase();
    pp2.setProductPurchaseId(2L);
    pp2.setQuantity(2);
    pp2.setProduct(product2);

    List<ProductPurchase> ppList = new ArrayList<ProductPurchase>();
    ppList.add(pp);
    ppList.add(pp2);

    Purchase purchase = new Purchase();
    purchase.setId(1L);
    purchase.setProductPurchases(ppList);

    when(sCart.getPurchase()).thenReturn(purchase);

    when(purchaseService.save(purchase)).thenReturn(purchase);

    mockMvc.perform(MockMvcRequestBuilders.post("/cart/empty")).andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/product/"))
        .andExpect(flash().attributeExists("flash"));
  }

  @Test
  public void emptyInvalidCartTest() throws Exception {

    when(sCart.getPurchase()).thenReturn(null);

    mockMvc.perform(MockMvcRequestBuilders.post("/cart/empty")).andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/error"));
  }

  @Test(expected = ProductOrderQuantityExceedsAvailabilityException.class)
  public void productOrderQuantityExceedsAvailabilityThrowsException() throws Exception {
    Product orderProduct = productBuilder();
    when(productService.findById(1L)).thenReturn(orderProduct);
    doAnswer(invocation -> {
      Product product = (Product) invocation.getArguments()[0];
      Integer quantity = (Integer) invocation.getArguments()[1];
      if (quantity > product.getQuantity()) {
        throw new ProductOrderQuantityExceedsAvailabilityException(product, quantity);
      }
      return null;
    }).when(productService).checkQuantity(any(Product.class), any(Integer.class));
    productService.checkQuantity(orderProduct, 10);
  }

  @Test
  public void subtotalDisplayedInCartButton() throws Exception {
    Product product = productBuilder();
    when(productService.findById(1L)).thenReturn(product);
    Purchase purchase = purchaseBuilder(product);
    when(sCart.getPurchase()).thenReturn(purchase);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/cart"))
        .andExpect(model().attributeExists("subTotal"));
  }

  private Product productBuilder() {
    Product product = new Product();
    product.setId(1L);
    product.setDesc("TestDesc");
    product.setName("TestName");
    product.setPrice(new BigDecimal(1.99));
    product.setQuantity(3);
    product.setFullImageName("imagename");
    product.setThumbImageName("imagename");
    return product;
  }

  private Purchase purchaseBuilder(Product product) {
    ProductPurchase pp = new ProductPurchase();
    pp.setProductPurchaseId(1L);
    pp.setQuantity(1);
    pp.setProduct(product);
    List<ProductPurchase> ppList = new ArrayList<ProductPurchase>();
    ppList.add(pp);

    Purchase purchase = new Purchase();
    purchase.setId(1L);
    purchase.setProductPurchases(ppList);
    return purchase;
  }
}
